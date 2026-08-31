package dev.aditya.orderservice.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderPaymentUpdateDTO {
    private Long orderId;
    private String paymentStatus;
    private Long paymentId;
    private String paymentMethod; // Card,Net_Banking,UPI,COD
    private Long totalAmount;
    private String paymentGateway;
}
