package Projet.Microservice.Services;

import Projet.Microservice.DTO.CreatePaymentRequestDTO;
import Projet.Microservice.DTO.PaymentRedirectDTO;
import Projet.Microservice.DTO.PaymentResponseDTO;
import Projet.Microservice.Entities.Currency;
import Projet.Microservice.Entities.Payment;
import Projet.Microservice.Entities.PaymentMethod;
import Projet.Microservice.Entities.PaymentStatus;
import Projet.Microservice.Entities.UserEntities.UserEntity;
import Projet.Microservice.Exceptions.BookingAlreadyPaidException;
import Projet.Microservice.Exceptions.PaymentNotFoundException;
import Projet.Microservice.Exceptions.PaymentStatusException;
import Projet.Microservice.Exceptions.UnsupportedCurrencyException;
import Projet.Microservice.Repositories.PaymentRepository;
import Projet.Microservice.Services.PaymentProviders.PaymentProviderFactory;
import Projet.Microservice.Services.PaymentProviders.PaymentProviderService;
import Projet.Microservice.Services.PaymentProviders.PayPalPaymentService;
import Projet.Microservice.Services.UserService.UserService;
import com.paypal.orders.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final CurrencyConversionService conversionService;
    private final PaymentProviderFactory providerFactory;
    private final PayPalPaymentService payPalPaymentService;
    private final UserService userService;

    // CREATE a new payment
    @Transactional
    public PaymentResponseDTO createPayment(CreatePaymentRequestDTO request) {
        validateCreatePaymentRequest(request);

        // Get the current authenticated user
        UserEntity user = userService.getCurrentUser();

        if (paymentRepository.existsByBookingId(request.getBookingId())) {
            throw new BookingAlreadyPaidException(request.getBookingId());
        }

        // Check for pending payments using user ID
        if (paymentRepository.existsByUser_IdAndPaymentStatusIn(
                user.getId(),
                List.of(PaymentStatus.CREATED, PaymentStatus.PENDING))) {
            throw new PaymentStatusException(
                    "You already have a pending payment. Please complete it before creating a new one.");
        }

        // Check currency availability
        List<PaymentMethod> availableMethods = providerFactory.getAvailableMethods(request.getCurrency().name());
        if (!availableMethods.contains(request.getPaymentMethod())) {
            throw new UnsupportedCurrencyException(request.getCurrency().name());
        }

        Payment payment = Payment.builder()
                .paymentId(generatePaymentId())
                .bookingId(request.getBookingId())
                .user(user) // SET USER ENTITY instead of userId string
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .paymentMethod(request.getPaymentMethod())
                .description(request.getDescription())
                .paymentStatus(PaymentStatus.CREATED)
                .expiresAt(Instant.now().plusSeconds(1800))
                .amountInUSD(convertToUSD(request.getAmount(), request.getCurrency()))
                .build();

        payment = paymentRepository.save(payment);
        log.info("Payment created successfully: {}", payment.getPaymentId());

        return mapToResponseDTO(payment);
    }
@Transactional
public PaymentRedirectDTO initiatePayment(String paymentId) {
    Payment payment = paymentRepository.findByPaymentId(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException(paymentId));

    if (payment.getPaymentStatus() != PaymentStatus.CREATED) {
        // Retourner un DTO avec le statut actuel au lieu de lever une exception
        return PaymentRedirectDTO.builder()
                .paymentId(paymentId)
                .status(payment.getPaymentStatus().name())
                .message("Le paiement ne peut pas être initié. Statut actuel: " + payment.getPaymentStatus())
                .build();
    }

    if (payment.getExpiresAt().isBefore(Instant.now())) {
        payment.setPaymentStatus(PaymentStatus.EXPIRED);
        paymentRepository.save(payment);
        return PaymentRedirectDTO.builder()
                .paymentId(paymentId)
                .status(PaymentStatus.EXPIRED.name())
                .message("Le paiement a expiré")
                .build();
    }

    PaymentProviderService provider = providerFactory.getProvider(payment.getPaymentMethod());
    PaymentRedirectDTO redirect = provider.processPayment(payment);

    payment.setPaymentStatus(PaymentStatus.PENDING);
    paymentRepository.save(payment);

    log.info("Payment initiated successfully: {}", paymentId);
    return redirect;
}

    @Transactional
    public PaymentResponseDTO handlePayPalSuccess(String paymentId, String token, String payerId) {
        Payment payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));



        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("Invalid payment status for capture: " + payment.getPaymentStatus());
        }


        try {
            // Capture the PayPal payment using the stored provider transaction ID
            Order capturedOrder = payPalPaymentService.capturePayment(payment.getProviderTransactionId());

            // Check if capture was successful
            if ("COMPLETED".equals(capturedOrder.status())) {
                payment.setPaymentStatus(PaymentStatus.COMPLETED);
                payment.setProcessedAt(Instant.now());
                payment.setPaymentToken(token);

                log.info("PayPal payment captured successfully. PaymentId: {}, OrderId: {}",
                        paymentId, payment.getProviderTransactionId());
            } else {
                payment.setPaymentStatus(PaymentStatus.FAILED);
                payment.setFailureReason("PayPal capture failed. Order status: " + capturedOrder.status());

                log.error("PayPal payment capture failed. PaymentId: {}, Status: {}",
                        paymentId, capturedOrder.status());
            }

            paymentRepository.save(payment);
            return mapToResponseDTO(payment);

        } catch (IOException e) {
            log.error("Error capturing PayPal payment: {}", paymentId, e);
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Technical error during payment capture: " + e.getMessage());
            paymentRepository.save(payment);
            throw new RuntimeException("Failed to capture PayPal payment", e);
        }
    }

    @Transactional
    public PaymentResponseDTO handlePayPalCancel(String paymentId) {
        Payment payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        payment.setPaymentStatus(PaymentStatus.CANCELLED);
        payment.setFailureReason("Payment cancelled by user");
        paymentRepository.save(payment);

        log.info("PayPal payment cancelled by user: {}", paymentId);
        return mapToResponseDTO(payment);
    }

    // GET a payment by ID
    @Transactional(readOnly = true)
    public PaymentResponseDTO getPayment(String paymentId) {
        Payment payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
        return mapToResponseDTO(payment);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponseDTO> getUserPayments(Pageable pageable) {
        // Get current user's ID from UserService
        Long userId = userService.getCurrentAppUserId();
        Page<Payment> payments = paymentRepository.findByUser_IdOrderByCreatedAtDesc(userId, pageable);
        return payments.map(this::mapToResponseDTO);
    }

    // ==== Helper Methods ====

    private void validateCreatePaymentRequest(CreatePaymentRequestDTO request) {
        // Remove userId validation - it comes from JWT token

        if (request.getBookingId() == null || request.getBookingId().trim().isEmpty()) {
            throw new IllegalArgumentException("Booking ID is required");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (request.getCurrency() == null) {
            throw new IllegalArgumentException("Currency is required");
        }
        if (request.getPaymentMethod() == null) {
            throw new IllegalArgumentException("Payment method is required");
        }
    }

    private String generatePaymentId() {
        return "PAY-" + System.currentTimeMillis() + "-" +
                String.valueOf(Math.random()).substring(2, 8);
    }

    private BigDecimal convertToUSD(BigDecimal amount, Currency currency) {
        if (currency == Currency.USD) return amount;
        return conversionService.convert(amount, currency, Currency.USD);
    }

    private PaymentResponseDTO mapToResponseDTO(Payment payment) {
        return PaymentResponseDTO.builder()
                .paymentId(payment.getPaymentId())
                .bookingId(payment.getBookingId())
                .userId(payment.getUser().getId().toString()) // Convert to String for DTO
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .description(payment.getDescription())
                .failureReason(payment.getFailureReason())
                .processedAt(payment.getProcessedAt())
                .expiresAt(payment.getExpiresAt())
                .amountInUSD(payment.getAmountInUSD())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .providerTransactionId(payment.getProviderTransactionId())
                .build();
    }
}