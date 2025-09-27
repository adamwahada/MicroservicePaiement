package Projet.Microservice.Entities;

import lombok.Getter;

@Getter
public enum PaymentMethod {

    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),

    PAYPAL("PayPal"),
    STRIPE("Stripe"),

    BANK_TRANSFER("Bank Transfer"),

    GIFT_CARD("Gift Card"),
    LOYALTY_POINTS("Loyalty Points");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

}
