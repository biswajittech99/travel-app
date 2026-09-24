package com.example.priceTrackerservice.scheduler;

import com.example.priceTrackerservice.service.PriceTrackerService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PricePollingScheduler {
    private final PriceTrackerService trackerService;

    public PricePollingScheduler(PriceTrackerService trackerService) {
        this.trackerService = trackerService;
    }

    @Scheduled(fixedRate = 300000) // every 5 minutes
    public void pollPrices() {
        trackerService.checkAllPrices(); // ✅ CORRECT METHOD
    }
}
