package com.brokerage.order_api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.brokerage.order_api.exception.GlobalExceptionHandler;
import com.brokerage.order_api.model.Asset;
import com.brokerage.order_api.model.Customer;
import com.brokerage.order_api.model.OrderStatus;
import com.brokerage.order_api.repository.CustomerRepository;
import com.brokerage.order_api.security.AuthUtil;
import com.brokerage.order_api.service.AssetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class AssetControllerTest {
    
    private MockMvc mockMvc;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AssetService assetService;

    private AssetController assetController;
    private Customer customer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        assetController = new AssetController(assetService, customerRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(assetController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        customer = Customer.builder().id(1L).username("testuser").build();
    }

    @Test
    void listAssets_Success() throws Exception {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        List<Asset> assets = List.of(Asset.builder().id(1L).customer(customer).build());
        when(assetService.listAssets(any())).thenReturn(assets);
        try (MockedStatic<AuthUtil> authUtil = Mockito.mockStatic(AuthUtil.class)) {
            authUtil.when(() -> AuthUtil.isAuthorizedForCustomer(any())).thenReturn(true);
            mockMvc.perform(get("/assets")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON)
                    .param("customerId", "1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].id").value(1));
        }
    }

    @Test
    void listAssets_CustomerNotFound() throws Exception {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/assets")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("customerId", "1"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void listAssets_Forbidden() throws Exception {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        try (MockedStatic<AuthUtil> authUtil = Mockito.mockStatic(AuthUtil.class)) {
            authUtil.when(() -> AuthUtil.isAuthorizedForCustomer(any())).thenReturn(false);
            mockMvc.perform(get("/assets")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("customerId", "1"))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    void listAssets_ServiceThrows() throws Exception {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        try (MockedStatic<AuthUtil> authUtil = Mockito.mockStatic(AuthUtil.class)) {
            authUtil.when(() -> AuthUtil.isAuthorizedForCustomer(any())).thenReturn(true);
            doThrow(new RuntimeException("Service error")).when(assetService).listAssets(any());
            mockMvc.perform(get("/assets")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("customerId", "1"))
                    .andDo(print())
                    .andExpect(status().isInternalServerError());
        }
    }
}
