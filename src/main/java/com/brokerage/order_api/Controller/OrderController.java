package com.brokerage.order_api.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.brokerage.order_api.dto.ApiResponse;
import com.brokerage.order_api.exception.EntityNotFoundException;
import com.brokerage.order_api.model.Customer;
import com.brokerage.order_api.model.Order;
import com.brokerage.order_api.model.OrderSide;
import com.brokerage.order_api.repository.CustomerRepository;
import com.brokerage.order_api.repository.OrderRepository;
import com.brokerage.order_api.security.AuthUtil;
import com.brokerage.order_api.service.OrderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

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

        if (!AuthUtil.isAuthorizedForCustomer(customer)) {
            throw new AccessDeniedException("You are not authorized to create orders.");
        }

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
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        Customer customer = customerRepository.findById(customerId)
        .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        if (!AuthUtil.isAuthorizedForCustomer(customer)) {
            throw new AccessDeniedException("You are not authorized to list orders.");
        }

        List<Order> orders = orderService.listOrders(
            customer, 
            start.atStartOfDay(ZoneId.systemDefault()).toInstant(), 
            end.atStartOfDay(ZoneId.systemDefault()).toInstant());
        return ResponseEntity.ok(
            ApiResponse.<List<Order>>builder()
                .success(true)
                .data(orders)
                .build()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteOrder(
            @PathVariable("id") Long orderId
    ) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new EntityNotFoundException("Order not found"));
            
        if (!AuthUtil.isAuthorizedForCustomer(order.getCustomer())) {
            throw new AccessDeniedException("You are not authorized to delete this order.");
        }
        
        orderService.deleteOrder(orderId);
        return ResponseEntity.ok(
            ApiResponse.<Void>builder()
                .success(true)
                .build()
        );
    }
    
    @PreAuthorize("hasRole('admin')")
    @PostMapping("/{id}/match") 
    public ResponseEntity<ApiResponse<Void>> matchOrder(
            @PathVariable("id") Long orderId
    ) {
        orderService.matchOrder(orderId);
        return ResponseEntity.ok(
            ApiResponse.<Void>builder()
                .success(true)
                .build()
        );
    }
}
