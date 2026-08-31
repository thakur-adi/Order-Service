package dev.aditya.orderservice.Model;

import dev.aditya.orderservice.DTO.OrderResponseDTO;
import dev.aditya.orderservice.DTO.ProductResponseDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "orders")//This has to be added as "Order" is a special keyword in MYSQL("ORDER BY"), which will result in failure of creation of table.
public class Order extends Base{
    private Long userId;
    @ManyToMany
    //Unfortunately there needs to be a relation between these 2 classes as its a list and RelationalDB can't store lists as a value(violates 1-nf). So we have to make product also an entity and define a relation between these 2 entities.
    private List<Product> products;
    private Double totalAmount;
    private String deliveryAddress;
    private Long paymentId;
    private String paymentMethod;
    private String paymentGateway;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

     public OrderResponseDTO convertToOrderDto(){
         OrderResponseDTO orderResponseDTO = new OrderResponseDTO();
         List<ProductResponseDTO> productResponseDTOS = new ArrayList<>();
         for(Product p:products){
             productResponseDTOS.add(p.convertToDto());
         }
         orderResponseDTO.setProducts(productResponseDTOS);
         orderResponseDTO.setOrderStatus(orderStatus.name());
         orderResponseDTO.setOrderCreationDate(getCreatedAt().toString());
         orderResponseDTO.setTotalAmount(totalAmount);
         orderResponseDTO.setPaymentMethod(paymentMethod);
         orderResponseDTO.setPaymentGateway(paymentGateway);
         orderResponseDTO.setDeliveryAddress(deliveryAddress);
         orderResponseDTO.setOrderLastUpdateDate(getLastUpdatedAt().toString());
         return orderResponseDTO;
     }
}
