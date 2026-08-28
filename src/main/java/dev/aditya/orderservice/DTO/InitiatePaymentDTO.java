package dev.aditya.orderservice.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InitiatePaymentDTO {
    private Long orderId;
    private Double amount;
    private Long userId;
    private String username;
    private String phoneNumber;
    private String userEmail;
}
