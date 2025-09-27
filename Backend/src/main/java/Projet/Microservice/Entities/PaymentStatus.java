package Projet.Microservice.Entities;

public enum PaymentStatus {
    CREATED,     // Payment record created, not yet initiated with provider
    PENDING,     // Payment initiated with provider, awaiting user action
    COMPLETED,   // Payment successfully completed
    FAILED,      // Payment failed
    CANCELLED,   // Payment cancelled by user
    EXPIRED,     // Payment expired (timeout)
    REFUNDED,    // Payment refunded (if you implement refunds)
    PARTIALLY_REFUNDED // Partial refund (if you implement partial refunds)
}