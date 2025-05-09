package com.brokerage.order_api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.brokerage.order_api.dto.ApiResponse;
import com.brokerage.order_api.exception.EntityNotFoundException;
import com.brokerage.order_api.model.Asset;
import com.brokerage.order_api.model.Customer;
import com.brokerage.order_api.repository.CustomerRepository;
import com.brokerage.order_api.security.AuthUtil;
import com.brokerage.order_api.service.AssetService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/assets")
@RequiredArgsConstructor
@Slf4j
public class AssetController {

    private final AssetService assetService;
    private final CustomerRepository customerRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Asset>>> listAssets(@RequestParam Long customerId) {

        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        if (!AuthUtil.isAuthorizedForCustomer(customer)) {
            throw new AccessDeniedException("You are not authorized to access these assets.");
        }

        List<Asset> assets = assetService.listAssets(customer);

        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponse.<List<Asset>>builder()
                .success(true)
                .data(assets)
                .build());
    }
}
