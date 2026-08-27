package dev.aditya.orderservice.Model;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
public class Order extends Base{
    private Long userId;
    private List<Product> products;
    private Double totalAmount;
    private String deliveryAddress;
    private String paymentMethod;
    private OrderStatus orderStatus;

}
