package Projet.Microservice.Exceptions;

public class PayPalApiException extends RuntimeException {
    public PayPalApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
