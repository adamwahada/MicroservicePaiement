package Projet.Microservice.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transaction_hisotry")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransactionHistory extends BaseEntity {

    @Column(name = "payment_id", nullable = false)
    private String paymentId;

    @Column(name = "transaction_type", nullable = false)
    private String transactionType; // PAYMENT, REFUND

    @Column(name = "old_status")
    private String oldStatus;

    @Column(name = "new_status")
    private String newStatus;

    @Column(name = "provider_response", columnDefinition = "TEXT")
    private String providerResponse; // JSON response from payment provider

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "transaction_timestamp")
    private LocalDateTime transactionTimes;
}