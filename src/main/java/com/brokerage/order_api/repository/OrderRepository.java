package com.brokerage.order_api.repository;

import com.brokerage.order_api.model.Order;
import com.brokerage.order_api.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order,Long> {
    List<Order> findByCustomerIdAndCreatedAtBetween(Long customerId, Instant start, Instant end);
    Optional<Order> findByIdAndStatus(Long id, OrderStatus status);
    List<Order> findByCustomerId(Long customerId);

}
