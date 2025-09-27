package Projet.Microservice.DTO;

import Projet.Microservice.Entities.Currency;
import Projet.Microservice.Entities.PaymentMethod;
import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Data
@Builder
public class CreatePaymentRequestDTO {

    @NotNull(message = "Booking ID is required")
    @Size(min = 1, message = "Booking ID cannot be empty")
    private String bookingId;

    @NotNull(message = "User ID is required")
    @Size(min = 1, message = "User ID cannot be empty")
    private String userId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Currency is required")
    private Currency currency;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @Size(max = 255, message = "Description cannot exceed 350 characters")
    private String description;
}