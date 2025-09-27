package Projet.Microservice.Controllers.PaymentProviders;

import Projet.Microservice.DTO.PaymentResponseDTO;
import Projet.Microservice.Services.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;


@RestController
@RequestMapping("/api/payments/paypal")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "PayPal Callbacks", description = "Endpoints called by PayPal for payment completion")
public class PayPalCallbackController {

    private final PaymentService paymentService;

    // Add a default value so it works even without the property set
    @Value("${app.base-url}")
    private String frontendUrl;

    @GetMapping("/success")
    @Operation(
            summary = "PayPal success callback",
            description = "Called by PayPal when user successfully approves payment"
    )
    public RedirectView handlePayPalSuccess(
            @Parameter(description = "Payment ID from your system")
            @RequestParam("paymentId") String paymentId,
            @Parameter(description = "PayPal payment token")
            @RequestParam("token") String token,
            @Parameter(description = "PayPal payer ID")
            @RequestParam("PayerID") String payerId) {

        log.info("PayPal SUCCESS callback received - PaymentId: {}, Token: {}, PayerID: {}",
                paymentId, token, payerId);

        try {
            // Capture the payment and update status
            PaymentResponseDTO payment = paymentService.handlePayPalSuccess(paymentId, token, payerId);

            // Redirect user to static success page
            String redirectUrl = frontendUrl + "/payment/success.html" +  // Added .html
                    "?paymentId=" + paymentId +
                    "&status=" + payment.getPaymentStatus() +
                    "&bookingId=" + payment.getBookingId();

            log.info("Redirecting user to success page: {}", redirectUrl);
            return new RedirectView(redirectUrl);

        } catch (Exception e) {
            log.error("Error processing PayPal success callback for payment: {}", paymentId, e);

            // Redirect to static error page
            String errorUrl = frontendUrl + "/payment/error.html" +  // Added .html
                    "?paymentId=" + paymentId +
                    "&error=" + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);

            return new RedirectView(errorUrl);
        }
    }

    /**
     * PAYPAL CANCEL CALLBACK
     *
     * WHEN CALLED: PayPal redirects here when user cancels payment
     * CALLED BY: PayPal (not your frontend)
     *
     * URL FORMAT: /api/payments/paypal/cancel?paymentId=PAY-123&token=EC-456
     *
     * FLOW:
     * 1. User clicks "Cancel" on PayPal payment page
     * 2. PayPal calls this endpoint
     * 3. This updates payment status to CANCELLED
     * 4. Redirects user back to your frontend cancel page
     */
    @GetMapping("/cancel")
    @Operation(
            summary = "PayPal cancel callback",
            description = "Called by PayPal when user cancels payment"
    )
    public RedirectView handlePayPalCancel(
            @Parameter(description = "Payment ID from your system")
            @RequestParam("paymentId") String paymentId,
            @Parameter(description = "PayPal payment token")
            @RequestParam(value = "token", required = false) String token) {

        log.info("PayPal CANCEL callback received - PaymentId: {}, Token: {}", paymentId, token);

        try {
            // Update payment status to cancelled
            PaymentResponseDTO payment = paymentService.handlePayPalCancel(paymentId);

            // Redirect user to frontend cancel page
            String redirectUrl = frontendUrl + "/payment/cancelled.html" +
                    "?paymentId=" + paymentId +
                    "&bookingId=" + payment.getBookingId();

            log.info("Redirecting user to cancel page: {}", redirectUrl);
            return new RedirectView(redirectUrl);

        } catch (Exception e) {
            log.error("Error processing PayPal cancel callback for payment: {}", paymentId, e);

            // Still redirect to cancel page, but with error info
            String cancelUrl = frontendUrl + "/payment/cancelled.html" +
                    "?paymentId=" + paymentId +
                    "&error=" + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);

            return new RedirectView(cancelUrl);
        }
    }

    /**
     * PAYMENT STATUS CHECK API
     *
     * WHEN CALLED: Frontend polling for payment status updates
     * CALLED BY: Your frontend JavaScript
     *
     * USE CASE:
     * - While user is on PayPal, frontend can poll this to detect completion
     * - After redirect, verify final status
     * - Real-time status updates on payment pages
     */
    @GetMapping("/status/{paymentId}")
    @Operation(
            summary = "Get real-time payment status",
            description = "Check current payment status - used for polling during payment process"
    )
    public ResponseEntity<PaymentResponseDTO> getPaymentStatus(
            @Parameter(description = "Payment ID to check")
            @PathVariable String paymentId) {

        log.debug("Status check requested for payment: {}", paymentId);

        try {
            PaymentResponseDTO payment = paymentService.getPayment(paymentId);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            log.error("Error getting payment status for: {}", paymentId, e);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/webhook")
    @Operation(
            summary = "PayPal webhook handler",
            description = "Handles PayPal webhook notifications (future enhancement)"
    )
    public ResponseEntity<String> handlePayPalWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "PAYPAL-TRANSMISSION-ID", required = false) String transmissionId,
            @RequestHeader(value = "PAYPAL-CERT-ID", required = false) String certId,
            @RequestHeader(value = "PAYPAL-TRANSMISSION-SIG", required = false) String signature) {


        // TODO: Implement webhook verification and processing
        // 1. Verify webhook signature
        // 2. Parse webhook payload
        // 3. Update payment status based on webhook event
        // 4. Send notifications if needed

        return ResponseEntity.ok("Webhook received");
    }
}