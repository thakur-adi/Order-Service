package dev.aditya.orderservice.Service;

import dev.aditya.orderservice.DTO.NewOrderRequestDTO;
import dev.aditya.orderservice.Model.Order;
import dev.aditya.orderservice.Model.OrderStatus;
import dev.aditya.orderservice.Model.Product;
import dev.aditya.orderservice.Model.User;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IOrderService {
    String generateNewOrder(User user, String deliveryAddress, List<Product> products, String paymentMethod, Double totalAmount);

    Page<Order> getOrderHistory(Long userId, int pageNumber,int pageSize);

    Order getOrderDetails(Long userId,Long orderId);

    void updateOrderPaymentDetails(Long orderId, String orderStatus, Long paymentId, String paymentMethod,String paymentGateway);
}
