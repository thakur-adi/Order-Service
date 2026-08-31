package dev.aditya.orderservice.Exceptions;

public class CustomPaymentGenerationException extends RuntimeException {
    public CustomPaymentGenerationException(String message) {
        super(message);
    }
}
