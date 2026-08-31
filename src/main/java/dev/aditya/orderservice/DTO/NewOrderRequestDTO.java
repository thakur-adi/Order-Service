package dev.aditya.orderservice.DTO;

import dev.aditya.orderservice.Model.Product;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class NewOrderRequestDTO {
    private List<Product> products;
    private Double totalAmount;
    private String deliveryAddress;
    private String paymentMethod;
}
