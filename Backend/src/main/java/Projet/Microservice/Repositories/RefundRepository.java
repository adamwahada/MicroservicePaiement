package Projet.Microservice.Repositories;
import Projet.Microservice.Entities.Refund;
import Projet.Microservice.Entities.RefundStatus;
import Projet.Microservice.Entities.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {

    Optional<Refund> findByRefundId(String refundId);

    List<Refund> findByPaymentOrderByCreatedAtDesc(Payment payment);

    List<Refund> findByRefundStatus(RefundStatus refundStatus);

    List<Refund> findByRefundStatusAndCreatedAtBefore(
            RefundStatus status, Instant createdBefore);

    Optional<Refund> findByProviderRefundId(String providerRefundId);

    // Calculate total refunded amount for a payment
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM Refund r WHERE r.payment = :payment AND r.refundStatus = :status")
    BigDecimal getTotalRefundedAmount(@Param("payment") Payment payment,
                                      @Param("status") RefundStatus status);


    // Check if payment has pending refunds
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Refund r WHERE r.payment = :payment AND r.refundStatus IN :pendingStatuses")
    boolean existsByPaymentAndPendingStatus(
            @Param("payment") Payment payment,
            @Param("pendingStatuses") List<RefundStatus> pendingStatuses);
}