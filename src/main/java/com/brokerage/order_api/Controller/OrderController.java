package com.brokerage.order_api.Controller;

import com.brokerage.order_api.model.Customer;
import com.brokerage.order_api.model.Order;
import com.brokerage.order_api.model.OrderSide;
import com.brokerage.order_api.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import com.brokerage.order_api.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.brokerage.order_api.dto.ApiResponse;
import com.brokerage.order_api.exception.EntityNotFoundException;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final CustomerRepository customerRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createOrder(
            @RequestParam Long customerId,
            @RequestParam String assetName,
            @RequestParam OrderSide side,
            @RequestParam BigDecimal size,
            @RequestParam BigDecimal price
    ) {
        Customer customer = customerRepository.findById(customerId)
        .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        orderService.createOrder(customer, assetName, side, size, price);
        return ResponseEntity.ok(
            ApiResponse.<Void>builder()
                .success(true)
                .data(null)
                .build()
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Order>>> listOrder(
            @RequestParam Long customerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end
    ) {
        Customer customer = customerRepository.findById(customerId)
        .orElseThrow(() -> new EntityNotFoundException("Customer not found"));
        List<Order> orders = orderService.listOrders(customer, start, end);
        return ResponseEntity.ok(
            ApiResponse.<List<Order>>builder()
                .success(true)
                .data(orders)
                .build()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteOrder(
            @RequestParam Long orderId
    ) {
        orderService.deleteOrder(orderId);
        return ResponseEntity.ok(
            ApiResponse.<Void>builder()
                .success(true)
                .build()
        );
    }

    @PostMapping("/{id}/match") 
    public ResponseEntity<ApiResponse<Void>> matchOrder(
            @RequestParam Long orderId
    ) {
        orderService.matchOrder(orderId);
        return ResponseEntity.ok(
            ApiResponse.<Void>builder()
                .success(true)
                .build()
        );
    }
}
