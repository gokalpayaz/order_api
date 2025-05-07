package com.brokerage.order_api.repository;

import com.brokerage.order_api.model.Asset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset,Long> {
    List<Asset> findByCustomerId(Long customerId);
    Optional<Asset> findByCustomerIdAndName(Long customerId, String name);
}
