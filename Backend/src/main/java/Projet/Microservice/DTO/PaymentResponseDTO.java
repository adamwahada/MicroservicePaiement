package Projet.Microservice.DTO;

import Projet.Microservice.Entities.Currency;
import Projet.Microservice.Entities.PaymentMethod;
import Projet.Microservice.Entities.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class PaymentResponseDTO {

    private String paymentId;
    private String bookingId;
    private String userId;
    private BigDecimal amount;
    private Currency currency;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String description;
    private String failureReason;
    private Instant processedAt;
    private Instant expiresAt;
    private BigDecimal amountInUSD;
    private Instant createdAt;
    private Instant updatedAt;
    private String providerTransactionId; // ← PayPal transaction ID

}
