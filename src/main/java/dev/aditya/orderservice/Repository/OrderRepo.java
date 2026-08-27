package dev.aditya.orderservice.Repository;

import dev.aditya.orderservice.Model.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepo extends JpaRepository<Order,Long> {
    Optional<Order> findById(Long id);
    List<Order> findAllByUserId(Long userId, Pageable pageable);
}
