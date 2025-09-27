package Projet.Microservice.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentRedirectDTO {
    private String paymentId;
    private String redirectUrl;
    private String providerOrderId;
    private String status;
    private String message;
}
