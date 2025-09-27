package Projet.Microservice.Exceptions;

public class BookingAlreadyPaidException extends RuntimeException {
    public BookingAlreadyPaidException(String bookingId) {
        super("A payment for booking ID '" + bookingId + "' already exists.");
    }
}