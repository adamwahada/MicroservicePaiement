package Projet.Microservice.Entities;

import Projet.Microservice.Entities.UserEntities.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {

    @Column(name = "payment_id", unique = true, nullable = false)
    private String paymentId;

    @Column(name = "booking_id", nullable = false,unique = true)
    private String bookingId;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 3)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @Column(name = "provider_transaction_id")
    private String providerTransactionId;

    @Column(name = "payment_token")
    private String paymentToken;

    @Column(name = "description")
    private String description;

    @Column(name = "failure_reason")
    private String failureReason;

    // Payment-specific timestamps (also UTC)
    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "amount_in_usd", precision = 10, scale = 2)
    private BigDecimal amountInUSD;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Refund> refunds;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
}
