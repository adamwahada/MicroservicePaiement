package Projet.Microservice.DTO;

import Projet.Microservice.Entities.PaymentMethod;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentGatewayRequestDTO {

    private String paymentId;
    private BigDecimal amount;
    private String currency;
    private PaymentMethod paymentMethod;
    private String paymentToken;
    private String description;
    private String customerEmail;
}