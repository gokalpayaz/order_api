package com.brokerage.order_api.controller;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.brokerage.order_api.exception.EntityNotFoundException;
import com.brokerage.order_api.exception.GlobalExceptionHandler;
import com.brokerage.order_api.model.Customer;
import com.brokerage.order_api.model.Order;
import com.brokerage.order_api.model.OrderStatus;
import com.brokerage.order_api.repository.CustomerRepository;
import com.brokerage.order_api.security.AuthUtil;
import com.brokerage.order_api.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

class OrderControllerTest {

    private MockMvc mockMvc;
    
    @Mock
    private OrderService orderService;

    @Mock
    private CustomerRepository customerRepository;

    private OrderController orderController;
    private Customer customer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        
        orderController = new OrderController(orderService, customerRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        
        customer = Customer.builder().id(1L).username("testuser").build();
    }

    @Test
    void createOrder_Success() throws Exception {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        try (MockedStatic<AuthUtil> authUtil = Mockito.mockStatic(AuthUtil.class)) {
            authUtil.when(() -> AuthUtil.isAuthorizedForCustomer(any())).thenReturn(true);
            mockMvc.perform(post("/orders")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON)
                    .param("customerId", "1")
                    .param("assetName", "AAPL")
                    .param("side", "BUY")
                    .param("size", "10")
                    .param("price", "5"))
                    .andDo(print())
                    .andExpect(status().isOk());
        }
    }

    @Test
    void createOrder_CustomerNotFound() throws Exception {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());
        try (MockedStatic<AuthUtil> authUtil = Mockito.mockStatic(AuthUtil.class)) {
            mockMvc.perform(post("/orders")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON)
                    .param("customerId", "1")
                    .param("assetName", "AAPL")
                    .param("side", "BUY")
                    .param("size", "10")
                    .param("price", "5"))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    void createOrder_Forbidden() throws Exception {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        try (MockedStatic<AuthUtil> authUtil = Mockito.mockStatic(AuthUtil.class)) {
            authUtil.when(() -> AuthUtil.isAuthorizedForCustomer(any())).thenReturn(false);
            mockMvc.perform(post("/orders")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("customerId", "1")
                    .param("assetName", "AAPL")
                    .param("side", "BUY")
                    .param("size", "10")
                    .param("price", "5"))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    void createOrder_ServiceThrows() throws Exception {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        try (MockedStatic<AuthUtil> authUtil = Mockito.mockStatic(AuthUtil.class)) {
            authUtil.when(() -> AuthUtil.isAuthorizedForCustomer(any())).thenReturn(true);
            doThrow(new RuntimeException("Service error")).when(orderService)
                    .createOrder(any(), anyString(), any(), any(), any());
            mockMvc.perform(post("/orders")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("customerId", "1")
                    .param("assetName", "AAPL")
                    .param("side", "BUY")
                    .param("size", "10")
                    .param("price", "5"))
                    .andDo(print())
                    .andExpect(status().isInternalServerError());
        }
    }

    @Test
    void listOrder_Success() throws Exception {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        List<Order> orders = List.of(Order.builder().id(1L).customer(customer).status(OrderStatus.PENDING).build());
        when(orderService.listOrders(any(), any(), any())).thenReturn(orders);
        try (MockedStatic<AuthUtil> authUtil = Mockito.mockStatic(AuthUtil.class)) {
            authUtil.when(() -> AuthUtil.isAuthorizedForCustomer(any())).thenReturn(true);
            mockMvc.perform(get("/orders")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON)
                    .param("customerId", "1")
                    .param("start", "2024-01-01")
                    .param("end", "2024-01-02"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].id").value(1));
        }
    }

    @Test
    void listOrder_CustomerNotFound() throws Exception {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/orders")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("customerId", "1")
                .param("start", "2024-01-01")
                .param("end", "2024-01-02"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void listOrder_Forbidden() throws Exception {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        try (MockedStatic<AuthUtil> authUtil = Mockito.mockStatic(AuthUtil.class)) {
            authUtil.when(() -> AuthUtil.isAuthorizedForCustomer(any())).thenReturn(false);
            mockMvc.perform(get("/orders")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("customerId", "1")
                    .param("start", "2024-01-01")
                    .param("end", "2024-01-02"))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    void listOrder_ServiceThrows() throws Exception {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        try (MockedStatic<AuthUtil> authUtil = Mockito.mockStatic(AuthUtil.class)) {
            authUtil.when(() -> AuthUtil.isAuthorizedForCustomer(any())).thenReturn(true);
            doThrow(new RuntimeException("Service error")).when(orderService)
                    .listOrders(any(), any(), any());
            mockMvc.perform(get("/orders")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("customerId", "1")
                    .param("start", "2024-01-01")
                    .param("end", "2024-01-02"))
                    .andDo(print())
                    .andExpect(status().isInternalServerError());
        }
    }

    @Test
    void deleteOrder_Success() throws Exception {
        // PreAuthorize is not enforced in this test setup
        mockMvc.perform(delete("/orders/1")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deleteOrder_ServiceThrows() throws Exception {
        doThrow(new EntityNotFoundException("No eligible order found")).when(orderService)
                .deleteOrder(anyLong());
        mockMvc.perform(delete("/orders/1")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void matchOrder_Success() throws Exception {
        // PreAuthorize is not enforced in this test setup
        mockMvc.perform(post("/orders/1/match")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void matchOrder_ServiceThrows() throws Exception {
        doThrow(new EntityNotFoundException("No eligible order found")).when(orderService)
                .matchOrder(anyLong());
        mockMvc.perform(post("/orders/1/match")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
} 