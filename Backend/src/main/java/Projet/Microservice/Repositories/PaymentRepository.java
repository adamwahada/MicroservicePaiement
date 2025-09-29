package Projet.Microservice.Repositories;

import Projet.Microservice.Entities.Payment;
import Projet.Microservice.Entities.PaymentStatus;
import Projet.Microservice.Entities.PaymentMethod;
import Projet.Microservice.Entities.Currency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    boolean existsByBookingId(String bookingId);

    // Basic finders - Perfect as they are!
    Optional<Payment> findByPaymentId(String paymentId);
    List<Payment> findByBookingId(String bookingId);
    Page<Payment> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    List<Payment> findByPaymentStatus(PaymentStatus paymentStatus);
    List<Payment> findByPaymentStatusInAndCreatedAtBefore(
            List<PaymentStatus> statuses, Instant createdBefore);
    List<Payment> findByPaymentStatusAndCreatedAtBefore(PaymentStatus status, Instant createdBefore);
    boolean existsByUserIdAndPaymentStatusIn(String userId, Collection<PaymentStatus> paymentStatus);



    // Provider integration queries - Essential for webhooks
    Optional<Payment> findByProviderTransactionId(String providerTransactionId);

    // Business intelligence queries - Great for reporting
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.userId = :userId AND p.paymentStatus = :status")
    long countByUserIdAndStatus(@Param("userId") String userId,
                                @Param("status") PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.userId = :userId AND p.paymentStatus IN :successStatuses ORDER BY p.createdAt DESC")
    List<Payment> findSuccessfulPaymentsByUserId(
            @Param("userId") String userId,
            @Param("successStatuses") List<PaymentStatus> successStatuses);

    // Fraud detection & analytics - Very useful
    List<Payment> findByAmountBetweenAndPaymentStatus(
            BigDecimal minAmount, BigDecimal maxAmount, PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.paymentMethod = :method AND p.createdAt BETWEEN :startDate AND :endDate")
    List<Payment> findByMethodAndDateRange(
            @Param("method") PaymentMethod method,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    // Booking validation - Critical for business logic
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Payment p WHERE p.bookingId = :bookingId AND p.paymentStatus IN :successStatuses")
    boolean existsByBookingIdAndSuccessfulStatus(
            @Param("bookingId") String bookingId,
            @Param("successStatuses") List<PaymentStatus> successStatuses);

    // Find payments by currency (useful for multi-currency platform)
    List<Payment> findByCurrencyAndPaymentStatus(Currency currency, PaymentStatus status);

    // Find expired payments for cleanup jobs
    @Query("SELECT p FROM Payment p WHERE p.expiresAt < :now AND p.paymentStatus = :status")
    List<Payment> findExpiredPayments(@Param("now") Instant now, @Param("status") PaymentStatus status);

    // Get total amount by user (for spending analysis)
    @Query("SELECT COALESCE(SUM(p.amountInUSD), 0) FROM Payment p WHERE p.userId = :userId AND p.paymentStatus IN :successStatuses")
    BigDecimal getTotalSpentByUser(@Param("userId") String userId,
                                   @Param("successStatuses") List<PaymentStatus> successStatuses);

    // Find recent payments for a user (dashboard)
    @Query("SELECT p FROM Payment p WHERE p.userId = :userId ORDER BY p.createdAt DESC LIMIT :limit")
    List<Payment> findRecentPaymentsByUser(@Param("userId") String userId, @Param("limit") int limit);
}