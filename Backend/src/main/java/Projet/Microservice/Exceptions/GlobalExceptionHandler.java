package Projet.Microservice.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BookingAlreadyPaidException.class)
    public ResponseEntity<String> handleBookingAlreadyPaid(BookingAlreadyPaidException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidPaymentMethodException.class)
    public ResponseEntity<String> handleCustomInvalidPaymentMethod(InvalidPaymentMethodException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<String> handlePaymentNotFound(PaymentNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleInvalidEnum(HttpMessageNotReadableException ex) {
        // Check if the cause is a failure to convert enum
        if (ex.getCause() instanceof com.fasterxml.jackson.databind.exc.InvalidFormatException invalidFormat) {
            if (invalidFormat.getTargetType().isEnum()) {
                String value = invalidFormat.getValue().toString();
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new InvalidPaymentMethodException(value).getMessage());
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(PaymentStatusException.class)
    public ResponseEntity<String> handlePaymentStatusException(PaymentStatusException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(UnsupportedCurrencyException.class)
    public ResponseEntity<Map<String, Object>> handleUnsupportedCurrency(UnsupportedCurrencyException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "UNSUPPORTED_CURRENCY");
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PayPalApiException.class)
    public ResponseEntity<Map<String, Object>> handlePayPalApi(PayPalApiException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "PAYPAL_API_ERROR");
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_GATEWAY);
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientBalance(
            InsufficientBalanceException ex, WebRequest request) {

        Map<String, Object> errorResponse = createBaseErrorResponse(
                "INSUFFICIENT_BALANCE",
                ex.getMessage(),
                request
        );


        // Add specific balance details
        errorResponse.put("details", Map.of(
                "required", ex.getRequiredAmount(),
                "current", ex.getCurrentBalance(),
                "userId", ex.getUserId(),
                "shortage", calculateShortage(ex.getRequiredAmount(), ex.getCurrentBalance())
        ));

        // Add user-friendly suggestions
        errorResponse.put("suggestions", Map.of(
                "action", "Please add funds to your account",
                "minimumRequired", ex.getRequiredAmount()
        ));

        System.out.println("❌ [EXCEPTION_HANDLER] Insufficient balance for user " +
                ex.getUserId() + ": required=" + ex.getRequiredAmount() +
                ", current=" + ex.getCurrentBalance());

        // Return 422 (Unprocessable Entity) instead of 400 for business rule violations
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse);
    }

    private Map<String, Object> createBaseErrorResponse(String errorCode, String message, WebRequest request) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", errorCode);
        errorResponse.put("message", message);
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));
        return errorResponse;
    }

    private String calculateShortage(String required, String current) {
        try {
            double requiredAmount = Double.parseDouble(required);
            double currentAmount = Double.parseDouble(current);
            double shortage = requiredAmount - currentAmount;
            return String.format("%.2f", Math.max(0, shortage));
        } catch (NumberFormatException e) {
            return "0.00";
        }
    }


}