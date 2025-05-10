package com.brokerage.order_api.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.brokerage.order_api.exception.EntityNotFoundException;
import com.brokerage.order_api.exception.InsufficientFundsException;
import com.brokerage.order_api.model.Asset;
import com.brokerage.order_api.model.Customer;
import com.brokerage.order_api.model.Order;
import com.brokerage.order_api.model.OrderSide;
import com.brokerage.order_api.model.OrderStatus;
import com.brokerage.order_api.repository.AssetRepository;
import com.brokerage.order_api.repository.OrderRepository;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private AssetRepository assetRepository;
    @InjectMocks
    private OrderService orderService;

    private Customer customer;
    private Asset depositAsset;
    private Asset targetAsset;
    @BeforeEach
    void setUp() {
        customer = Customer.builder().id(1L).build();
        depositAsset = Asset.builder()
                .id(1L)
                .name("TRY")
                .customer(customer)
                .size(new BigDecimal("1000"))
                .usableSize(new BigDecimal("1000"))
                .build();
        targetAsset = Asset.builder()
                .id(2L)
                .name("AAPL")
                .customer(customer)
                .size(new BigDecimal("100"))
                .usableSize(new BigDecimal("100"))
                .build();
    }

    @Test
    void testCreateOrder_Buy_Success() {
        // Arrange
        String assetName = "AAPL";
        BigDecimal orderSize = new BigDecimal("10");
        BigDecimal price = new BigDecimal("5");

        when(assetRepository.findByCustomerIdAndName(customer.getId(), "TRY"))
                .thenReturn(Optional.of(depositAsset));
        when(assetRepository.findByCustomerIdAndName(customer.getId(), assetName))
                .thenReturn(Optional.empty());
        when(assetRepository.save(any(Asset.class))).thenAnswer(i -> i.getArguments()[0]);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        assertDoesNotThrow(() -> orderService.createOrder(customer, assetName, OrderSide.BUY, orderSize, price));

        // Assert
        verify(assetRepository).save(any(Asset.class));
        verify(orderRepository).save(any(Order.class));
        assertEquals(new BigDecimal("100"), targetAsset.getUsableSize());
        assertEquals(new BigDecimal("100"), targetAsset.getSize());
        assertEquals(new BigDecimal("950"), depositAsset.getUsableSize());
        assertEquals(new BigDecimal("1000"), depositAsset.getSize());
    }
    
    @Test
    void testCreateOrder_Buy_EntityNotFoundException() {
        // Arrange
        String assetName = "AAPL";
        BigDecimal orderSize = new BigDecimal("10");
        BigDecimal price = new BigDecimal("5");
        
        when(assetRepository.findByCustomerIdAndName(customer.getId(), "TRY"))
            .thenReturn(Optional.empty());

        // Act
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> 
            orderService.createOrder(customer, assetName, OrderSide.BUY, orderSize, price));

        // Assert
        assertEquals("TRY asset was not found", exception.getMessage());
        verify(assetRepository, never()).save(any(Asset.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testCreateOrder_Buy_InsufficientFunds() {
        // Arrange
        String assetName = "AAPL";
        BigDecimal orderSize = new BigDecimal("100");
        BigDecimal price = new BigDecimal("50");
        
        lenient().when(assetRepository.findByCustomerIdAndName(customer.getId(), "TRY"))
                .thenReturn(Optional.of(depositAsset));
        lenient().when(assetRepository.findByCustomerIdAndName(customer.getId(), assetName))
                .thenReturn(Optional.empty());

    
        // Act
        InsufficientFundsException exception = assertThrows(InsufficientFundsException.class, () -> 
            orderService.createOrder(customer, assetName, OrderSide.BUY, orderSize, price)
        );
    
        // Assert
        assertEquals("Insufficient TRY funds to place the order", exception.getMessage());
        verify(assetRepository, never()).save(any(Asset.class));
        verify(orderRepository, never()).save(any(Order.class));
    }


    @Test
    void testCreateOrder_Sell_Success() {
        // Arrange
        String assetName = "AAPL";
        BigDecimal orderSize = new BigDecimal("10");
        BigDecimal price = new BigDecimal("5");

        when(assetRepository.findByCustomerIdAndName(customer.getId(), "TRY"))
            .thenReturn(Optional.of(depositAsset));
        when(assetRepository.findByCustomerIdAndName(customer.getId(), assetName))
            .thenReturn(Optional.of(targetAsset));
        when(assetRepository.save(any(Asset.class))).thenAnswer(i -> i.getArguments()[0]);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        assertDoesNotThrow(() -> orderService.createOrder(customer, assetName, OrderSide.SELL, orderSize, price));

        // Assert
        verify(assetRepository).save(any(Asset.class));
        verify(orderRepository).save(any(Order.class));
        assertEquals(new BigDecimal("90"), targetAsset.getUsableSize());
        assertEquals(new BigDecimal("100"), targetAsset.getSize());
        assertEquals(new BigDecimal("1000"), depositAsset.getUsableSize());
        assertEquals(new BigDecimal("1000"), depositAsset.getSize());
    }

    @Test
    void testCreateOrder_Sell_InsufficientFunds() {
        // Arrange
        String assetName = "AAPL";
        BigDecimal orderSize = new BigDecimal("200");
        BigDecimal price = new BigDecimal("5");
        
        when(assetRepository.findByCustomerIdAndName(customer.getId(), "TRY"))
            .thenReturn(Optional.of(depositAsset));
        when(assetRepository.findByCustomerIdAndName(customer.getId(), assetName))
            .thenReturn(Optional.of(targetAsset));

        // Act
        InsufficientFundsException exception = assertThrows(InsufficientFundsException.class, () -> 
            orderService.createOrder(customer, assetName, OrderSide.SELL, orderSize, price));

        // Assert
        assertEquals("Insufficient stocks to place the order", exception.getMessage());
        verify(assetRepository, never()).save(any(Asset.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testCreateOrder_Sell_EntityNotFoundException() {
        // Arrange
        String assetName = "AAPL";
        BigDecimal orderSize = new BigDecimal("10");
        BigDecimal price = new BigDecimal("5");
        
        when(assetRepository.findByCustomerIdAndName(customer.getId(), "TRY"))
            .thenReturn(Optional.of(depositAsset));
        when(assetRepository.findByCustomerIdAndName(customer.getId(), assetName))
            .thenReturn(Optional.empty());

        // Act
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> 
            orderService.createOrder(customer, assetName, OrderSide.SELL, orderSize, price));

        // Assert
        assertEquals("Insufficient stocks to place the order", exception.getMessage());
        verify(assetRepository, never()).save(any(Asset.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testCreateOrder_InvalidOrderSide() {
        // Arrange
        String assetName = "AAPL";
        BigDecimal orderSize = new BigDecimal("10");
        BigDecimal price = new BigDecimal("5");
        lenient().when(assetRepository.findByCustomerIdAndName(customer.getId(), "TRY"))
            .thenReturn(Optional.of(depositAsset));
        lenient().when(assetRepository.findByCustomerIdAndName(customer.getId(), assetName))
            .thenReturn(Optional.empty());

        // Act
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            orderService.createOrder(customer, assetName, null, orderSize, price));

        // Assert
        assertEquals("Invalid Order Side", exception.getMessage());
        verify(assetRepository, never()).save(any(Asset.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testListOrders_ReturnsList() {
        // Arrange
        Instant start = Instant.parse("2024-01-01T00:00:00Z");
        Instant end = Instant.parse("2024-01-02T00:00:00Z");
        Order order = Order.builder().id(1L).customer(customer).createdAt(start.plusSeconds(3600)).build();
        when(orderRepository.findByCustomerIdAndCreatedAtBetween(customer.getId(), start, end))
            .thenReturn(List.of(order));

        // Act
        List<Order> result = orderService.listOrders(customer, start, end);

        // Assert
        assertEquals(1, result.size());
        assertEquals(order, result.get(0));
    }

    @Test
    void testListOrders_EmptyList() {
        // Arrange
        Instant start = Instant.parse("2024-01-01T00:00:00Z");
        Instant end = Instant.parse("2024-01-02T00:00:00Z");
        when(orderRepository.findByCustomerIdAndCreatedAtBetween(customer.getId(), start, end))
            .thenReturn(List.of());

        // Act
        List<Order> result = orderService.listOrders(customer, start, end);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testListOrders_StartNotBeforeEnd() {
        // Arrange
        Instant start = Instant.parse("2024-01-02T00:00:00Z");
        Instant end = Instant.parse("2024-01-01T00:00:00Z");

        // Act
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            orderService.listOrders(customer, start, end));

        // Assert
        assertEquals("Start time must be before end time", exception.getMessage());
    }

    @Test
    void testDeleteOrder_Success_Buy() {
        // Arrange  
        Order order = Order.builder()
            .id(1L)
            .side(OrderSide.BUY)
            .size(new BigDecimal("10"))
            .price(new BigDecimal("5"))
            .status(OrderStatus.PENDING)
            .customer(customer)
            .asset(targetAsset)
            .build();
        when(orderRepository.findByIdAndStatus(1L, OrderStatus.PENDING)).thenReturn(Optional.of(order));
        when(assetRepository.findByCustomerIdAndName(customer.getId(), "TRY")).thenReturn(Optional.of(depositAsset));
        when(assetRepository.save(any(Asset.class))).thenAnswer(i -> i.getArguments()[0]);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        assertDoesNotThrow(() -> orderService.deleteOrder(1L));

        // Assert
        assertEquals(OrderStatus.CANCELED, order.getStatus());
        verify(assetRepository).save(any(Asset.class));
        verify(orderRepository).save(any(Order.class));
        assertEquals(new BigDecimal("1050"), depositAsset.getUsableSize());
        assertEquals(new BigDecimal("1000"), depositAsset.getSize());
        assertEquals(new BigDecimal("100"), targetAsset.getUsableSize());
        assertEquals(new BigDecimal("100"), targetAsset.getSize());
    }

    @Test
    void testDeleteOrder_Success_Sell() {
        // Arrange
        Order order = Order.builder()
            .id(1L)
            .side(OrderSide.SELL)
            .size(new BigDecimal("10"))
            .price(new BigDecimal("5"))
            .status(OrderStatus.PENDING)
            .customer(customer)
            .asset(targetAsset)
            .build();
        when(orderRepository.findByIdAndStatus(1L, OrderStatus.PENDING)).thenReturn(Optional.of(order));
        when(assetRepository.findByCustomerIdAndName(customer.getId(), "TRY")).thenReturn(Optional.of(depositAsset));
        when(assetRepository.save(any(Asset.class))).thenAnswer(i -> i.getArguments()[0]);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act  
        assertDoesNotThrow(() -> orderService.deleteOrder(1L));

        // Assert
        assertEquals(OrderStatus.CANCELED, order.getStatus());
        verify(assetRepository).save(any(Asset.class));
        verify(orderRepository).save(any(Order.class));
        assertEquals(new BigDecimal("1000"), depositAsset.getUsableSize());
        assertEquals(new BigDecimal("1000"), depositAsset.getSize());
        assertEquals(new BigDecimal("110"), targetAsset.getUsableSize());
        assertEquals(new BigDecimal("100"), targetAsset.getSize());
    }

    @Test
    void testDeleteOrder_EntityNotFound_Order() {
        // Arrange
        when(orderRepository.findByIdAndStatus(1L, OrderStatus.PENDING)).thenReturn(Optional.empty());

        // Act
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> orderService.deleteOrder(1L));

        // Assert
        assertEquals("No eligible order found", exception.getMessage());
    }

    @Test
    void testDeleteOrder_EntityNotFound_TryAsset() {
        // Arrange
        Order order = Order.builder()
            .id(1L) 
            .side(OrderSide.BUY)
            .size(new BigDecimal("10"))
            .price(new BigDecimal("5"))
            .status(OrderStatus.PENDING)
            .customer(customer)
            .asset(targetAsset)
            .build();
        when(orderRepository.findByIdAndStatus(1L, OrderStatus.PENDING)).thenReturn(Optional.of(order));
        when(assetRepository.findByCustomerIdAndName(customer.getId(), "TRY")).thenReturn(Optional.empty());

        // Act
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> orderService.deleteOrder(1L));

        // Assert
        assertEquals("TRY asset was not found", exception.getMessage());
    }

    @Test
    void testMatchOrder_Success_Buy() {
        // Arrange
        Order order = Order.builder()
            .id(1L)
            .side(OrderSide.BUY)
            .size(new BigDecimal("10"))
            .price(new BigDecimal("5"))
            .status(OrderStatus.PENDING)
            .customer(customer)
            .asset(targetAsset)
            .build();
        when(orderRepository.findByIdAndStatus(1L, OrderStatus.PENDING)).thenReturn(Optional.of(order));
        when(assetRepository.findByCustomerIdAndName(customer.getId(), "TRY")).thenReturn(Optional.of(depositAsset));
        when(assetRepository.save(any(Asset.class))).thenAnswer(i -> i.getArguments()[0]);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        assertDoesNotThrow(() -> orderService.matchOrder(1L));

        // Assert
        assertEquals(OrderStatus.MATCHED, order.getStatus());
        verify(assetRepository, times(2)).save(any(Asset.class));
        verify(orderRepository).save(any(Order.class));
        assertEquals(new BigDecimal("1000"), depositAsset.getUsableSize());
        assertEquals(new BigDecimal("950"), depositAsset.getSize());
        assertEquals(new BigDecimal("110"), targetAsset.getUsableSize());
        assertEquals(new BigDecimal("110"), targetAsset.getSize());
    }

    @Test
    void testMatchOrder_Success_Sell() {
        // Arrange
        Order order = Order.builder()
            .id(1L)
            .side(OrderSide.SELL)
            .size(new BigDecimal("10"))
            .price(new BigDecimal("5"))
            .status(OrderStatus.PENDING)
            .customer(customer)
            .asset(targetAsset)
            .build();
        when(orderRepository.findByIdAndStatus(1L, OrderStatus.PENDING)).thenReturn(Optional.of(order));
        when(assetRepository.findByCustomerIdAndName(customer.getId(), "TRY")).thenReturn(Optional.of(depositAsset));
        when(assetRepository.save(any(Asset.class))).thenAnswer(i -> i.getArguments()[0]);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        assertDoesNotThrow(() -> orderService.matchOrder(1L));

        // Assert
        assertEquals(OrderStatus.MATCHED, order.getStatus());
        verify(assetRepository, times(2)).save(any(Asset.class));
        verify(orderRepository).save(any(Order.class));
        assertEquals(new BigDecimal("1050"), depositAsset.getUsableSize());
        assertEquals(new BigDecimal("1050"), depositAsset.getSize());
        assertEquals(new BigDecimal("100"), targetAsset.getUsableSize());
        assertEquals(new BigDecimal("90"), targetAsset.getSize());
    }

    @Test
    void testMatchOrder_EntityNotFound_Order() {    
        // Arrange
        when(orderRepository.findByIdAndStatus(1L, OrderStatus.PENDING)).thenReturn(Optional.empty());

        // Act
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> 
            orderService.matchOrder(1L));

        // Assert
        assertEquals("No eligible order found", exception.getMessage());
    }

    @Test
    void testMatchOrder_EntityNotFound_TryAsset() {
        // Arrange
        Order order = Order.builder()
            .id(1L)
            .side(OrderSide.BUY)
            .size(new BigDecimal("10"))
            .price(new BigDecimal("5"))
            .status(OrderStatus.PENDING)
            .customer(customer)
            .asset(targetAsset)
            .build();
        when(orderRepository.findByIdAndStatus(1L, OrderStatus.PENDING)).thenReturn(Optional.of(order));
        when(assetRepository.findByCustomerIdAndName(customer.getId(), "TRY")).thenReturn(Optional.empty());

        // Act
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> orderService.matchOrder(1L));

        // Assert
        assertEquals("TRY asset not found", exception.getMessage());
    }

} 