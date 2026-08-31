package dev.aditya.orderservice.Repository;

import dev.aditya.orderservice.Model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepo extends JpaRepository<Order,Long> {
    @Override
    Optional<Order> findById(Long id);

    Optional<Order> findOrderByIdAndUserId(Long orderId,Long userId);

    Page<Order> findAllByUserId(Long userId, Pageable pageable);
}
