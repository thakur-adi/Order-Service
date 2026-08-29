package dev.aditya.orderservice.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

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
    private String paymentMethod;
    private OrderStatus orderStatus;
}
