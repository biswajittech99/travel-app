package com.example.priceTrackerservice.repository;

import com.example.priceTrackerservice.entity.PriceAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PriceAlertRepository extends JpaRepository<PriceAlert, String> {
    List<PriceAlert> findByActiveTrue();

    List<PriceAlert> findBySearchKeyAndActiveTrue(String searchKey);
}
