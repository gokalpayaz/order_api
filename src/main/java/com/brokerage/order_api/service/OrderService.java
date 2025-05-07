package com.brokerage.order_api.service;

import com.brokerage.order_api.model.*;
import com.brokerage.order_api.repository.AssetRepository;
import com.brokerage.order_api.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final AssetRepository assetRepository;

    @Transactional
    public void createOrder(Customer customer, String assetName, OrderSide orderSide, BigDecimal orderSize, BigDecimal price) {

        Asset depositAsset = assetRepository.findByCustomerIdAndName(customer.getId(), "TRY")
                .orElseThrow(() -> new RuntimeException("TRY asset was not found for the customer"));
        Asset targetAsset = new Asset();

        if (orderSide == OrderSide.BUY) {

            BigDecimal cost = price.multiply(orderSize);

            BigDecimal usableDeposit = depositAsset.getUsableSize();

            if (usableDeposit.compareTo(cost) < 0) {
                throw new RuntimeException("Insufficient TRY funds to place the order");
            }

            depositAsset.setUsableSize(depositAsset.getUsableSize().subtract(cost));

            Optional<Asset> assetToBuyOptional = assetRepository.findByCustomerIdAndName(customer.getId(), assetName);
            if (assetToBuyOptional.isPresent()) {
                targetAsset = assetToBuyOptional.get();
            } else {
                targetAsset = Asset.builder()
                        .customer(customer)
                        .name(assetName)
                        .size(BigDecimal.ZERO)
                        .usableSize(BigDecimal.ZERO)
                        .build();
                assetRepository.save(targetAsset);
            }
        } else if (orderSide == OrderSide.SELL) {
            targetAsset = assetRepository.findByCustomerIdAndName(customer.getId(), assetName)
                    .orElseThrow(() -> new RuntimeException("Insufficient stocks to place the order"));

            if (targetAsset.getSize().compareTo(orderSize) < 0) {
                throw new RuntimeException("Insufficient stocks to place the order");
            }
            targetAsset.setUsableSize(targetAsset.getUsableSize().subtract(orderSize));
            assetRepository.save(targetAsset);

        }
        else {
            throw new IllegalArgumentException("Invalid Order Side");
        }

        Order order = Order.builder()
                .side(orderSide)
                .size(orderSize)
                .price(price)
                .status(OrderStatus.PENDING)
                .customer(customer)
                .asset(targetAsset)
                .build();
        orderRepository.save(order);
    }

    public List<Order> listOrders (Customer customer, Instant start, Instant end){
        return orderRepository.findByCustomerIdAndCreatedAtBetween(customer.getId(),start,end);
    }

    @Transactional
    public void deleteOrder (long orderId) {
        Order order = orderRepository.findByIdAndStatus(orderId, OrderStatus.PENDING).orElseThrow(() -> new RuntimeException("No eligible order found"));
        OrderSide orderSide = order.getSide();
        BigDecimal orderSize = order.getSize();
        BigDecimal orderValue = orderSize.multiply(order.getPrice());
        Asset targetAsset = order.getAsset();
        Asset depositAsset = assetRepository.findByCustomerIdAndName(order.getCustomer().getId(), "TRY")
                .orElseThrow(() -> new RuntimeException("TRY asset was not found"));

        if (orderSide == OrderSide.BUY) {
            depositAsset.setUsableSize(depositAsset.getUsableSize().add(orderValue));
        } else {
            targetAsset.setUsableSize(targetAsset.getUsableSize().add(orderSize));
        }
        order.setStatus(OrderStatus.CANCELED);
    }

    @Transactional
    public void matchOrder(long orderId){
        Order order = orderRepository.findByIdAndStatus(orderId, OrderStatus.PENDING).orElseThrow(() -> new RuntimeException("Order not found"));
        OrderSide orderSide = order.getSide();
        BigDecimal orderSize = order.getSize();
        BigDecimal orderValue = orderSize.multiply(order.getPrice());
        Asset depositAsset = assetRepository.findByCustomerIdAndName(order.getCustomer().getId(), "TRY")
                .orElseThrow(() -> new RuntimeException("TRY asset not found"));
        Asset targetAsset = order.getAsset();

        if (orderSide == OrderSide.BUY) {
            // Finalize payment from TRY
            depositAsset.setSize(depositAsset.getSize().subtract(orderValue));
            // Grant stocks to customer
            targetAsset.setUsableSize(targetAsset.getUsableSize().add(orderSize));
            targetAsset.setSize(targetAsset.getSize().add(orderSize));
        } else {
            // Remove stocks from user
            targetAsset.setSize(targetAsset.getSize().subtract(orderSize));
            // Grant deposit to customer
            depositAsset.setUsableSize(depositAsset.getUsableSize().add(orderValue));
            depositAsset.setSize(depositAsset.getSize().add(orderValue));
        }
        order.setStatus(OrderStatus.MATCHED);

    }
}
