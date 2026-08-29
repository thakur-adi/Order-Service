package dev.aditya.orderservice.Repository;

import dev.aditya.orderservice.Model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepo extends JpaRepository<Product,Long> {
    Optional<Product> findProductByProductIdAndQuantityAndPrice(Long productId, Integer quantity, Double price);
}
