package dev.aditya.orderservice.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderPaymentUpdateDTO {
    private Long orderId;
    private String paymentStatus;
    private Long paymentId;
    private String paymentMethod;
    private Long totalAmount;
}
