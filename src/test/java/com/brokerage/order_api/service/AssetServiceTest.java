package com.brokerage.order_api.service;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.brokerage.order_api.model.Asset;
import com.brokerage.order_api.model.Customer;
import com.brokerage.order_api.repository.AssetRepository;

@ExtendWith(MockitoExtension.class)
public class AssetServiceTest {

    @Mock
    private AssetRepository assetRepository;
    @InjectMocks
    private AssetService assetService;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = Customer.builder().id(1L).build();
    }

    @Test
    void testListAssets_ReturnsAssets() {
        // Arrange
        Asset asset1 = Asset.builder().id(1L).name("TRY").customer(customer).build();
        Asset asset2 = Asset.builder().id(2L).name("AAPL").customer(customer).build();
        List<Asset> assets = Arrays.asList(asset1, asset2);
        when(assetRepository.findByCustomerId(customer.getId())).thenReturn(assets);

        // Act
        List<Asset> result = assetService.listAssets(customer);
        
        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(asset1));
        assertTrue(result.contains(asset2));
        verify(assetRepository).findByCustomerId(customer.getId());
    }
} 