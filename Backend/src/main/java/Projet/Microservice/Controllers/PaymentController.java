package Projet.Microservice.Controllers;

import Projet.Microservice.DTO.CreatePaymentRequestDTO;
import Projet.Microservice.DTO.PaymentRedirectDTO;
import Projet.Microservice.DTO.PaymentResponseDTO;
import Projet.Microservice.Services.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"${app.frontend-url}", "${app.admin-url:}"})
@Tag(name = "Payment Management", description = "APIs for managing payments across different services")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(
            summary = "Create payment record",
            description = "Creates a new payment record linked to a booking. Payment is not yet processed."
    )
    @ApiResponse(responseCode = "201", description = "Payment record created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid payment request")
    @ApiResponse(responseCode = "409", description = "Booking already has a payment")
    public ResponseEntity<PaymentResponseDTO> createPayment(
            @Valid @RequestBody CreatePaymentRequestDTO request) {

        log.info("Creating payment for booking: {} by user: {}",
                request.getBookingId(), request.getUserId());

        PaymentResponseDTO payment = paymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    /**
     * 2. INITIATE PAYMENT PROCESS
     *
     * WHEN TO USE: Immediately after createPayment() succeeds
     * TRIGGERED BY: Frontend automatically after payment creation
     *
     * FLOW:
     * - Takes the paymentId from step 1
     * - Creates order with payment provider (PayPal/Stripe/etc.)
     * - Returns redirect URL to send user to payment provider
     * - Payment status changes from CREATED → PENDING
     */
    @PostMapping("/{paymentId}/initiate")
    public ResponseEntity<PaymentRedirectDTO> initiatePayment(@PathVariable String paymentId) {
        log.info("Initiating payment process for: {}", paymentId);

        PaymentRedirectDTO redirect = paymentService.initiatePayment(paymentId);

        if (redirect.getRedirectUrl() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(redirect);
        }
        return ResponseEntity.ok(redirect);
    }
    /**
     * 3. GET PAYMENT DETAILS
     *
     * WHEN TO USE:
     * - Check payment status during/after payment process
     * - Display payment details to user
     * - Admin dashboard to view payment info
     *
     * TRIGGERED BY:
     * - Frontend polling during payment process
     * - User viewing payment history
     * - Admin checking payment status
     */
    @GetMapping("/{paymentId}")
    @Operation(
            summary = "Get payment details",
            description = "Retrieves complete payment information including current status"
    )
    @ApiResponse(responseCode = "200", description = "Payment details retrieved")
    @ApiResponse(responseCode = "404", description = "Payment not found")
    public ResponseEntity<PaymentResponseDTO> getPayment(
            @Parameter(description = "Payment ID")
            @PathVariable String paymentId) {

        log.debug("Retrieving payment details for: {}", paymentId);

        PaymentResponseDTO payment = paymentService.getPayment(paymentId);
        return ResponseEntity.ok(payment);
    }

    /**
     * 4. GET USER'S PAYMENT HISTORY
     *
     * WHEN TO USE:
     * - User dashboard showing payment history
     * - Customer service checking user's payments
     * - Generating user payment reports
     *
     * TRIGGERED BY:
     * - User viewing "My Payments" page
     * - Admin searching user payments
     */
    @GetMapping("/user/{userId}")
    @Operation(
            summary = "Get user's payment history",
            description = "Retrieves paginated list of user's payments, ordered by creation date (newest first)"
    )
    @ApiResponse(responseCode = "200", description = "Payment history retrieved")
    public ResponseEntity<Page<PaymentResponseDTO>> getUserPayments(
            @Parameter(description = "User ID")
            @PathVariable String userId,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page")
            @RequestParam(defaultValue = "10") int size) {

        log.debug("Retrieving payment history for user: {} (page: {}, size: {})",
                userId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<PaymentResponseDTO> payments = paymentService.getUserPayments(userId, pageable);
        return ResponseEntity.ok(payments);
    }

    /**
     * 5. CANCEL PAYMENT
     *
     * WHEN TO USE:
     * - User cancels payment before completing
     * - Admin cancels stuck payment
     * - System timeout cancels expired payment
     *
     * TRIGGERED BY:
     * - User clicking "Cancel" during payment
     * - PayPal cancel callback
     * - Admin action
     * - Scheduled job for expired payments
     */
    @PostMapping("/{paymentId}/cancel")
    @Operation(
            summary = "Cancel payment",
            description = "Cancels a payment that hasn't been completed yet"
    )
    @ApiResponse(responseCode = "200", description = "Payment cancelled successfully")
    @ApiResponse(responseCode = "400", description = "Payment cannot be cancelled (already completed)")
    @ApiResponse(responseCode = "404", description = "Payment not found")
    public ResponseEntity<PaymentResponseDTO> cancelPayment(
            @Parameter(description = "Payment ID to cancel")
            @PathVariable String paymentId) {

        log.info("Cancelling payment: {}", paymentId);

        PaymentResponseDTO payment = paymentService.handlePayPalCancel(paymentId);
        return ResponseEntity.ok(payment);
    }

    /**
     * 6. GET PAYMENTS BY BOOKING
     *
     * WHEN TO USE:
     * - Booking service checking if booking is paid
     * - Customer service checking booking payment status
     * - Refund processing
     *
     * TRIGGERED BY:
     * - Other microservices checking payment status
     * - Admin viewing booking details
     */
    @GetMapping("/booking/{bookingId}")
    @Operation(
            summary = "Get payment by booking ID",
            description = "Retrieves payment associated with a specific booking"
    )
    @ApiResponse(responseCode = "200", description = "Payment found for booking")
    @ApiResponse(responseCode = "404", description = "No payment found for this booking")
    public ResponseEntity<PaymentResponseDTO> getPaymentByBooking(
            @Parameter(description = "Booking ID")
            @PathVariable String bookingId) {

        log.debug("Retrieving payment for booking: {}", bookingId);

        // You'll need to add this method to your service
        // PaymentResponseDTO payment = paymentService.getPaymentByBookingId(bookingId);
        // return ResponseEntity.ok(payment);

        // For now, return not implemented
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    /**
     * 7. BULK PAYMENT STATUS CHECK
     *
     * WHEN TO USE:
     * - Admin dashboard showing multiple payment statuses
     * - Reporting and analytics
     * - Batch processing operations
     *
     * TRIGGERED BY:
     * - Admin dashboard loading
     * - Scheduled reporting jobs
     * - Bulk operations
     */
    @PostMapping("/bulk-status")
    @Operation(
            summary = "Check multiple payment statuses",
            description = "Retrieves status for multiple payments in one request"
    )
    @ApiResponse(responseCode = "200", description = "Payment statuses retrieved")
    public ResponseEntity<Page<PaymentResponseDTO>> getBulkPaymentStatus(
            @RequestBody @Parameter(description = "List of payment IDs")
            java.util.List<String> paymentIds,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        log.debug("Bulk status check for {} payments", paymentIds.size());

        // You'll need to implement this in your service
        // Page<PaymentResponseDTO> payments = paymentService.getBulkPaymentStatus(paymentIds, PageRequest.of(page, size));
        // return ResponseEntity.ok(payments);

        // For now, return not implemented
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    /**
     * 8. HEALTH CHECK ENDPOINT
     *
     * WHEN TO USE:
     * - Load balancer health checks
     * - Monitoring systems
     * - DevOps readiness probes
     *
     * TRIGGERED BY:
     * - Automated health monitoring
     * - Kubernetes readiness probes
     * - Load balancer checks
     */
    @GetMapping("/health")
    @Operation(
            summary = "Health check",
            description = "Simple health check endpoint for monitoring"
    )
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Payment service is running");
    }
}