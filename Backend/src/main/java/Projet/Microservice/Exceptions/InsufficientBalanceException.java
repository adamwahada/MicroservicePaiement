package Projet.Microservice.Exceptions;

import lombok.Getter;

// Create custom exception for insufficient balance
@Getter
public class InsufficientBalanceException extends RuntimeException {
    private final String userId;
    private final String requiredAmount;
    private final String currentBalance;

    public InsufficientBalanceException(
            String userId,
            String requiredAmount,
            String currentBalance,
            String message
    ) {
        super(message); // ✅ Now we control the message
        this.userId = userId;
        this.requiredAmount = requiredAmount;
        this.currentBalance = currentBalance;
    }
}

