package Projet.Microservice.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
}