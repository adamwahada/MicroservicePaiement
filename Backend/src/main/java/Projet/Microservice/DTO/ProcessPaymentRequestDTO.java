package Projet.Microservice.DTO;

import lombok.Builder;
import lombok.Data;
@Data
@Builder
public class ProcessPaymentRequestDTO {

    private String paymentToken;

    private String cardHolderName;
    private String billingAddress;
    private boolean savePaymentMethod;
}