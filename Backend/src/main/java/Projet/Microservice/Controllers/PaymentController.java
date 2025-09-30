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
            description = "Creates a new payment record linked to a booking for the authenticated user. Payment is not yet processed."
    )
    @ApiResponse(responseCode = "201", description = "Payment record created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid payment request")
    @ApiResponse(responseCode = "401", description = "User not authenticated")
    @ApiResponse(responseCode = "409", description = "Booking already has a payment")
    public ResponseEntity<PaymentResponseDTO> createPayment(
            @Valid @RequestBody CreatePaymentRequestDTO request) {

        // User ID is extracted from JWT token in the service layer
        log.info("Creating payment for booking: {}", request.getBookingId());

        PaymentResponseDTO payment = paymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    @PostMapping("/{paymentId}/initiate")
    @Operation(
            summary = "Initiate payment process",
            description = "Creates order with payment provider and returns redirect URL"
    )
    @ApiResponse(responseCode = "200", description = "Payment initiated successfully")
    @ApiResponse(responseCode = "400", description = "Payment cannot be initiated")
    @ApiResponse(responseCode = "401", description = "User not authenticated")
    @ApiResponse(responseCode = "404", description = "Payment not found")
    public ResponseEntity<PaymentRedirectDTO> initiatePayment(@PathVariable String paymentId) {
        log.info("Initiating payment process for: {}", paymentId);

        PaymentRedirectDTO redirect = paymentService.initiatePayment(paymentId);

        if (redirect.getRedirectUrl() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(redirect);
        }
        return ResponseEntity.ok(redirect);
    }

    @GetMapping("/{paymentId}")
    @Operation(
            summary = "Get payment details",
            description = "Retrieves complete payment information including current status"
    )
    @ApiResponse(responseCode = "200", description = "Payment details retrieved")
    @ApiResponse(responseCode = "401", description = "User not authenticated")
    @ApiResponse(responseCode = "404", description = "Payment not found")
    public ResponseEntity<PaymentResponseDTO> getPayment(
            @Parameter(description = "Payment ID")
            @PathVariable String paymentId) {

        log.debug("Retrieving payment details for: {}", paymentId);

        PaymentResponseDTO payment = paymentService.getPayment(paymentId);
        return ResponseEntity.ok(payment);
    }

    // CHANGED: Remove userId path parameter - get from authenticated user
    @GetMapping("/my-payments")
    @Operation(
            summary = "Get current user's payment history",
            description = "Retrieves paginated list of authenticated user's payments, ordered by creation date (newest first)"
    )
    @ApiResponse(responseCode = "200", description = "Payment history retrieved")
    @ApiResponse(responseCode = "401", description = "User not authenticated")
    public ResponseEntity<Page<PaymentResponseDTO>> getCurrentUserPayments(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page")
            @RequestParam(defaultValue = "10") int size) {

        log.debug("Retrieving payment history for authenticated user (page: {}, size: {})", page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<PaymentResponseDTO> payments = paymentService.getUserPayments(pageable);
        return ResponseEntity.ok(payments);
    }

    // OPTIONAL: Keep this for admin use only - add @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/user/{userId}")
    @Operation(
            summary = "[ADMIN] Get user's payment history",
            description = "Admin endpoint to retrieve any user's payment history"
    )
    @ApiResponse(responseCode = "200", description = "Payment history retrieved")
    @ApiResponse(responseCode = "401", description = "User not authenticated")
    @ApiResponse(responseCode = "403", description = "Not authorized - admin only")
    // Uncomment when you add Spring Security method security:
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<PaymentResponseDTO>> getUserPaymentsAdmin(
            @Parameter(description = "User ID")
            @PathVariable Long userId,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page")
            @RequestParam(defaultValue = "10") int size) {

        log.debug("Admin retrieving payment history for user: {} (page: {}, size: {})",
                userId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        // You'll need to add this method to PaymentService for admin use
        // Page<PaymentResponseDTO> payments = paymentService.getUserPaymentsByIdAdmin(userId, pageable);
        // return ResponseEntity.ok(payments);

        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PostMapping("/{paymentId}/cancel")
    @Operation(
            summary = "Cancel payment",
            description = "Cancels a payment that hasn't been completed yet"
    )
    @ApiResponse(responseCode = "200", description = "Payment cancelled successfully")
    @ApiResponse(responseCode = "400", description = "Payment cannot be cancelled (already completed)")
    @ApiResponse(responseCode = "401", description = "User not authenticated")
    @ApiResponse(responseCode = "404", description = "Payment not found")
    public ResponseEntity<PaymentResponseDTO> cancelPayment(
            @Parameter(description = "Payment ID to cancel")
            @PathVariable String paymentId) {

        log.info("Cancelling payment: {}", paymentId);

        PaymentResponseDTO payment = paymentService.handlePayPalCancel(paymentId);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/booking/{bookingId}")
    @Operation(
            summary = "Get payment by booking ID",
            description = "Retrieves payment associated with a specific booking"
    )
    @ApiResponse(responseCode = "200", description = "Payment found for booking")
    @ApiResponse(responseCode = "401", description = "User not authenticated")
    @ApiResponse(responseCode = "404", description = "No payment found for this booking")
    public ResponseEntity<PaymentResponseDTO> getPaymentByBooking(
            @Parameter(description = "Booking ID")
            @PathVariable String bookingId) {

        log.debug("Retrieving payment for booking: {}", bookingId);

        // You'll need to add this method to your service
        // PaymentResponseDTO payment = paymentService.getPaymentByBookingId(bookingId);
        // return ResponseEntity.ok(payment);

        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PostMapping("/bulk-status")
    @Operation(
            summary = "Check multiple payment statuses",
            description = "Retrieves status for multiple payments in one request"
    )
    @ApiResponse(responseCode = "200", description = "Payment statuses retrieved")
    @ApiResponse(responseCode = "401", description = "User not authenticated")
    public ResponseEntity<Page<PaymentResponseDTO>> getBulkPaymentStatus(
            @RequestBody @Parameter(description = "List of payment IDs")
            java.util.List<String> paymentIds,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        log.debug("Bulk status check for {} payments", paymentIds.size());

        // You'll need to implement this in your service
        // Page<PaymentResponseDTO> payments = paymentService.getBulkPaymentStatus(paymentIds, PageRequest.of(page, size));
        // return ResponseEntity.ok(payments);

        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @GetMapping("/health")
    @Operation(
            summary = "Health check",
            description = "Simple health check endpoint for monitoring"
    )
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Payment service is running");
    }
}