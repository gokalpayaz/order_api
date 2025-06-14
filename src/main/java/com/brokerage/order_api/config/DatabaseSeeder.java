package com.brokerage.order_api.config;


import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.brokerage.order_api.model.Asset;
import com.brokerage.order_api.model.Customer;
import com.brokerage.order_api.model.Order;
import com.brokerage.order_api.model.OrderSide;
import com.brokerage.order_api.model.OrderStatus;
import com.brokerage.order_api.repository.AssetRepository;
import com.brokerage.order_api.repository.CustomerRepository;
import com.brokerage.order_api.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final CustomerRepository customerRepository;
    private final AssetRepository assetRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (customerRepository.count() == 0) {
            Customer admin = Customer.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("adminPassword"))
                    .role("admin")
                    .build();

            Customer user1 = Customer.builder()
                    .username("user1")
                    .password(passwordEncoder.encode("user1Password"))
                    .role("user")
                    .build();

            Asset tryAsset = Asset.builder()
                    .customer(user1)
                    .name("TRY")
                    .size(new BigDecimal(1000))
                    .usableSize(new BigDecimal(1000))
                    .build();

            Asset appleAsset = Asset.builder()
                    .customer(user1)
                    .name("AAPL")
                    .size(new BigDecimal(3.21))
                    .usableSize(new BigDecimal(3.21))
                    .build();

            Order buyOrder = Order.builder()
                    .customer(user1)
                    .side(OrderSide.BUY)
                    .size(new BigDecimal(3.21))
                    .asset(appleAsset)
                    .price(new BigDecimal(250))
                    .status(OrderStatus.MATCHED)
                    .createdAt(Instant.now().minus(5, ChronoUnit.DAYS))
                    .build();

            customerRepository.save(admin);
            customerRepository.save(user1);
            assetRepository.save(tryAsset);
            assetRepository.save(appleAsset);
            orderRepository.save(buyOrder);

            log.info("Sample data seeded.");

        }


    }
}
