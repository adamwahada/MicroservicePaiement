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

    // Booking queries
    boolean existsByBookingId(String bookingId);
    Optional<Payment> findByPaymentId(String paymentId);
    List<Payment> findByBookingId(String bookingId);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Payment p WHERE p.bookingId = :bookingId AND p.paymentStatus IN :successStatuses")
    boolean existsByBookingIdAndSuccessfulStatus(
            @Param("bookingId") String bookingId,
            @Param("successStatuses") List<PaymentStatus> successStatuses);

    // User-based queries (using Long userId via relationship)
    Page<Payment> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    boolean existsByUser_IdAndPaymentStatusIn(Long userId, Collection<PaymentStatus> paymentStatus);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.user.id = :userId AND p.paymentStatus = :status")
    long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.user.id = :userId AND p.paymentStatus IN :successStatuses ORDER BY p.createdAt DESC")
    List<Payment> findSuccessfulPaymentsByUserId(
            @Param("userId") Long userId,
            @Param("successStatuses") List<PaymentStatus> successStatuses);

    @Query("SELECT COALESCE(SUM(p.amountInUSD), 0) FROM Payment p WHERE p.user.id = :userId AND p.paymentStatus IN :successStatuses")
    BigDecimal getTotalSpentByUser(@Param("userId") Long userId,
                                   @Param("successStatuses") List<PaymentStatus> successStatuses);

    @Query("SELECT p FROM Payment p WHERE p.user.id = :userId ORDER BY p.createdAt DESC LIMIT :limit")
    List<Payment> findRecentPaymentsByUser(@Param("userId") Long userId, @Param("limit") int limit);

    // Status-based queries
    List<Payment> findByPaymentStatus(PaymentStatus paymentStatus);
    List<Payment> findByPaymentStatusInAndCreatedAtBefore(
            List<PaymentStatus> statuses, Instant createdBefore);
    List<Payment> findByPaymentStatusAndCreatedAtBefore(PaymentStatus status, Instant createdBefore);

    // Provider integration queries
    Optional<Payment> findByProviderTransactionId(String providerTransactionId);

    // Fraud detection & analytics
    List<Payment> findByAmountBetweenAndPaymentStatus(
            BigDecimal minAmount, BigDecimal maxAmount, PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.paymentMethod = :method AND p.createdAt BETWEEN :startDate AND :endDate")
    List<Payment> findByMethodAndDateRange(
            @Param("method") PaymentMethod method,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    // Currency and expiration queries
    List<Payment> findByCurrencyAndPaymentStatus(Currency currency, PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.expiresAt < :now AND p.paymentStatus = :status")
    List<Payment> findExpiredPayments(@Param("now") Instant now, @Param("status") PaymentStatus status);
}