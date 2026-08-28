package dev.aditya.orderservice.Service;

import dev.aditya.orderservice.DTO.NewOrderRequestDTO;
import dev.aditya.orderservice.Model.Product;
import dev.aditya.orderservice.Model.User;

import java.util.List;

public interface IOrderService {
    String generateNewOrder(User user, String deliveryAddress, List<Product> products, String paymentMethod, Double totalAmount);
}
