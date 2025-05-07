package com.brokerage.order_api.service;

import com.brokerage.order_api.model.Asset;
import com.brokerage.order_api.model.Customer;
import com.brokerage.order_api.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetService {
    private final AssetRepository assetRepository;

    public List<Asset> listAssets(Customer customer){
        return assetRepository.findByCustomerId(customer.getId());
    }
}
