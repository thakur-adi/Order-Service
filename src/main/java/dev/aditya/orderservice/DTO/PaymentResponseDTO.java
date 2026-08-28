package dev.aditya.orderservice.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentResponseDTO {
    private Long orderId;
    private String paymentStatus;
    private String paymentId;
    private String paymentMethod;
    private Long totalAmount;
}
