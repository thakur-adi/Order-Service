package dev.aditya.orderservice.DTO;


import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class OrderResponseDTO {
    private String orderCreationDate;
    private String orderLastUpdateDate; //for "delivered on 18th june" type scenarios
    private String orderStatus;
    private List<ProductResponseDTO> products;
    private Double totalAmount;
    private String deliveryAddress;
    private String paymentMethod;

}
