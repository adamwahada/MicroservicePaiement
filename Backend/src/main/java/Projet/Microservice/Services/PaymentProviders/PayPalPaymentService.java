package Projet.Microservice.Services.PaymentProviders;

import Projet.Microservice.DTO.PaymentRedirectDTO;
import Projet.Microservice.Entities.Payment;
import Projet.Microservice.Entities.PaymentStatus;
import Projet.Microservice.Exceptions.PayPalApiException;
import Projet.Microservice.Exceptions.UnsupportedCurrencyException;
import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.http.exceptions.HttpException;
import com.paypal.orders.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class PayPalPaymentService implements PaymentProviderService {

    private final PayPalHttpClient payPalClient;

    @Value("${app.base-url}")
    private String baseUrl;

    public PayPalPaymentService(PayPalHttpClient payPalClient) {
        this.payPalClient = payPalClient;
    }

    @Override
    public PaymentRedirectDTO processPayment(Payment payment) {
        log.info("Processing PayPal payment for bookingId: {}", payment.getBookingId());

        String returnUrl = baseUrl + "/api/payments/paypal/success?paymentId=" + payment.getPaymentId();
        String cancelUrl = baseUrl + "/api/payments/paypal/cancel?paymentId=" + payment.getPaymentId();

        OrdersCreateRequest request = new OrdersCreateRequest();
        request.prefer("return=representation");
        request.requestBody(new OrderRequest()
                .checkoutPaymentIntent("CAPTURE")
                .applicationContext(new ApplicationContext()
                        .brandName("Your Travel Booking Service")
                        .returnUrl(returnUrl)
                        .cancelUrl(cancelUrl)
                        .landingPage("NO_PREFERENCE")
                        .userAction("PAY_NOW")
                )
                .purchaseUnits(List.of(new PurchaseUnitRequest()
                        .referenceId(payment.getBookingId())
                        .description(payment.getDescription() != null ?
                                payment.getDescription() :
                                "Booking payment for " + payment.getBookingId())
                        .amountWithBreakdown(new AmountWithBreakdown()
                                .currencyCode(payment.getCurrency().name())
                                .value(payment.getAmount().toString())
                        )
                ))
        );

        try {
            HttpResponse<Order> response = payPalClient.execute(request);
            Order order = response.result();

            // Save the PayPal order ID
            payment.setProviderTransactionId(order.id());
            payment.setPaymentStatus(PaymentStatus.PENDING);

            String approvalUrl = order.links().stream()
                    .filter(link -> "approve".equals(link.rel()))
                    .findFirst()
                    .map(LinkDescription::href)
                    .orElseThrow(() -> new PayPalApiException("No approval URL found in PayPal response", null));

            log.info("PayPal order created successfully. OrderId: {}, PaymentId: {}",
                    order.id(), payment.getPaymentId());

            return PaymentRedirectDTO.builder()
                    .paymentId(payment.getPaymentId())
                    .redirectUrl(approvalUrl)
                    .providerOrderId(order.id())
                    .build();

        } catch (HttpException e) {
            String errorMessage = e.getMessage();
            log.error("PayPal API error for paymentId {} -> {}", payment.getPaymentId(), errorMessage);

            // Handle all major PayPal errors
            if (errorMessage != null) {
                if (errorMessage.contains("CURRENCY_NOT_SUPPORTED")) {
                    payment.setPaymentStatus(PaymentStatus.FAILED);
                    payment.setFailureReason("Currency not supported: " + payment.getCurrency());
                    throw new UnsupportedCurrencyException(payment.getCurrency().name());

                } else if (errorMessage.contains("INSUFFICIENT_FUNDS")) {
                    payment.setPaymentStatus(PaymentStatus.FAILED);
                    payment.setFailureReason("Insufficient funds in PayPal account.");
                    throw new PayPalApiException("Payment declined due to insufficient funds.", e);

                } else if (errorMessage.contains("INSTRUMENT_DECLINED")) {
                    payment.setPaymentStatus(PaymentStatus.FAILED);
                    payment.setFailureReason("PayPal funding source declined.");
                    throw new PayPalApiException("Payment declined: funding source problem.", e);

                } else if (errorMessage.contains("PAYER_ACTION_REQUIRED")) {
                    payment.setPaymentStatus(PaymentStatus.FAILED);
                    payment.setFailureReason("User action required on PayPal account.");
                    throw new PayPalApiException("Payment requires user action.", e);

                } else {
                    payment.setPaymentStatus(PaymentStatus.FAILED);
                    payment.setFailureReason("PayPal API error: " + errorMessage);
                    throw new PayPalApiException("PayPal API error: " + errorMessage, e);
                }
            }
        } catch (IOException e) {
            log.error("PayPal payment creation failed for paymentId: {}", payment.getPaymentId(), e);
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.setFailureReason("PayPal order creation failed: " + e.getMessage());
            throw new PayPalApiException("Error creating PayPal order", e);
        }

        // Should never reach here
        throw new PayPalApiException("Unknown error occurred during PayPal payment processing", null);
    }

    public Order capturePayment(String paypalOrderId) throws IOException {
        log.info("Capturing PayPal payment for orderId: {}", paypalOrderId);

        OrdersCaptureRequest request = new OrdersCaptureRequest(paypalOrderId);
        request.requestBody(new OrderRequest());

        HttpResponse<Order> response = payPalClient.execute(request);
        return response.result();
    }
}
