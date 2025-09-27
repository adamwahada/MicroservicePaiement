package Projet.Microservice.Entities;

import lombok.Getter;
@Getter
public enum RefundStatus {

    REQUESTED("Refund requested by customer"),
    PENDING("Refund request is pending approval"),
    APPROVED("Refund approved, waiting for processing"),
    PROCESSING("Refund is being processed"),
    COMPLETED("Refund has been completed"),
    REJECTED("Refund request has been rejected");

    private final String description;

    RefundStatus(String description) {
        this.description = description;
    }

}
