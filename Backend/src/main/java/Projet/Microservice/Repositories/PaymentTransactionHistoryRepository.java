package Projet.Microservice.Repositories;

import Projet.Microservice.Entities.PaymentTransactionHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface PaymentTransactionHistoryRepository extends JpaRepository<PaymentTransactionHistory, Long> {

    List<PaymentTransactionHistory> findByPaymentIdOrderByTransactionTimes(String paymentId);

    List<PaymentTransactionHistory> findByTransactionType(String transactionType);

    // Find logs in date range (for debugging)
    Page<PaymentTransactionHistory> findByTransactionTimesBetween(
            Instant startDate, Instant endDate, Pageable pageable);

    // Count errors for a payment (health check)
    @Query("SELECT COUNT(l) FROM PaymentTransactionHistory l WHERE l.paymentId = :paymentId AND l.errorMessage IS NOT NULL")
    long countErrorsByPaymentId(@Param("paymentId") String paymentId);
}