package com.brokerage.order_api.Controller;

import com.brokerage.order_api.model.Customer;
import com.brokerage.order_api.model.Order;
import com.brokerage.order_api.model.OrderSide;
import com.brokerage.order_api.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import com.brokerage.order_api.service.OrderService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CustomerRepository customerRepository;

    @PostMapping
    public void createOrder(
            @RequestParam Long customerId,
            @RequestParam String assetName,
            @RequestParam OrderSide side,
            @RequestParam BigDecimal size,
            @RequestParam BigDecimal price
            ) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        orderService.createOrder(customer,assetName, side, size, price);
    }

    @GetMapping
    public List<Order> listOrder(
            @RequestParam Long customerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end
            ) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        return orderService.listOrders(customer, start, end);
    }

    @DeleteMapping("/{id}")
    public void deleteOrder(
            @RequestParam Long orderId
    ) {
        orderService.deleteOrder(orderId);
    }

    @PostMapping("/{id}/match")
    public void matchOrder(
            @RequestParam Long orderId
    ) {
        orderService.matchOrder(orderId);
    }
}
