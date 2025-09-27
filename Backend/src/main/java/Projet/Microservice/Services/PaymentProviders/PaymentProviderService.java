package Projet.Microservice.Services.PaymentProviders;

import Projet.Microservice.DTO.PaymentRedirectDTO;
import Projet.Microservice.Entities.Payment;

public interface PaymentProviderService {
    PaymentRedirectDTO processPayment(Payment payment);
}