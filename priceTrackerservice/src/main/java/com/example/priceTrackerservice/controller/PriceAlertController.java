package com.example.priceTrackerservice.controller;

import com.example.priceTrackerservice.entity.PriceAlert;
import com.example.priceTrackerservice.repository.PriceAlertRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/price-alerts")
public class PriceAlertController {
    private final PriceAlertRepository repo;

    public PriceAlertController(PriceAlertRepository repo) {
        this.repo = repo;
    }

    @PostMapping
    public ResponseEntity<PriceAlert> create(@RequestBody PriceAlert alert){
        return ResponseEntity.ok(repo.save(alert));
    }

    @GetMapping("/active")
    public List<PriceAlert> activeAlerts(){
        return repo.findByActiveTrue();
    }
}
