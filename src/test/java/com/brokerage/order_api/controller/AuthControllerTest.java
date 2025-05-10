package com.brokerage.order_api.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.brokerage.order_api.dto.LoginRequest;
import com.brokerage.order_api.exception.GlobalExceptionHandler;
import com.brokerage.order_api.model.Customer;
import com.brokerage.order_api.repository.CustomerRepository;
import com.brokerage.order_api.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

class AuthControllerTest {

    private MockMvc mockMvc;
    
    @Mock
    private JwtUtil jwtUtil;
    
    @Mock
    private CustomerRepository customerRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    private AuthController authController;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        objectMapper = new ObjectMapper();
        authController = new AuthController(jwtUtil, customerRepository, passwordEncoder);
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }
    
    @Test
    void login_Success() throws Exception {
        // Arrange
        Customer customer = Customer.builder()
                .id(1L)
                .username("testuser")
                .password("hashedPassword")
                .role("CUSTOMER")
                .build();
        
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");
        
        when(customerRepository.findByUsername("testuser")).thenReturn(customer);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("JWT_TOKEN");
        
        // Act & Assert
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login Successful"))
                .andExpect(jsonPath("$.data").value("JWT_TOKEN"));
    }
    
    @Test
    void login_InvalidCredentials() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("wrongpassword");
        
        when(customerRepository.findByUsername("testuser")).thenReturn(null);
        
        // Act & Assert
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid username or password"))
                .andExpect(jsonPath("$.data").isEmpty());
    }
    
    @Test
    void login_PasswordMismatch() throws Exception {
        // Arrange
        Customer customer = Customer.builder()
                .id(1L)
                .username("testuser")
                .password("hashedPassword")
                .role("user")
                .build();
        
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("wrongpassword");
        
        when(customerRepository.findByUsername("testuser")).thenReturn(customer);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        
        // Act & Assert
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid username or password"))
                .andExpect(jsonPath("$.data").isEmpty());
    }
    
    @Test
    void login_ServerError() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");
        
        when(customerRepository.findByUsername("testuser")).thenThrow(new RuntimeException("Database error"));
        
        // Act & Assert
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }
} 