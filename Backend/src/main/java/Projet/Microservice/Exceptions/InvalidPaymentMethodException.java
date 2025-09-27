package Projet.Microservice.Exceptions;

public class InvalidPaymentMethodException extends RuntimeException {

    public InvalidPaymentMethodException(String value) {
        super("Invalid payment method: '" + value + "'.") ;}
}