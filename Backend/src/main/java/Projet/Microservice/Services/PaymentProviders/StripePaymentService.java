package Projet.Microservice.Services.PaymentProviders;

import Projet.Microservice.DTO.PaymentRedirectDTO;
import Projet.Microservice.Entities.Payment;
import Projet.Microservice.Entities.PaymentStatus;
import Projet.Microservice.Exceptions.UnsupportedCurrencyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@Slf4j
public class StripePaymentService implements PaymentProviderService {

    private static final Set<String> SUPPORTED_CURRENCIES = Set.of("USD", "EUR", "GBP", "AUD", "CAD", "JPY");


    private void validateCurrency(String currency) {
        if (!SUPPORTED_CURRENCIES.contains(currency)) {
            throw new UnsupportedCurrencyException(currency);
        }
    }
    public static Set<String> getSupportedCurrencies() {
        return SUPPORTED_CURRENCIES;
    }

    @Override
    public PaymentRedirectDTO processPayment(Payment payment) {
        log.info("Processing Stripe payment for bookingId: {}", payment.getBookingId());

        // Validate the currency first
        validateCurrency(payment.getCurrency().name());

        // Set status to PENDING since the user still needs to complete the payment
        payment.setPaymentStatus(PaymentStatus.PENDING);

        // Generate a fake Stripe redirect URL for now (later integrate real Stripe Checkout)
        String redirectUrl = "https://stripe.com/checkout/session/" + System.currentTimeMillis();

        log.info("Stripe payment redirect URL generated for bookingId: {} -> {}",
                payment.getBookingId(), redirectUrl);

        return PaymentRedirectDTO.builder()
                .paymentId(payment.getPaymentId())
                .redirectUrl(redirectUrl)
                .build();
    }

}
