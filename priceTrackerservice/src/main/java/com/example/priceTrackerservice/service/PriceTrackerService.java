package com.example.priceTrackerservice.service;

import com.example.priceTrackerservice.client.SearchClient;
import com.example.priceTrackerservice.entity.PriceAlert;
import com.example.priceTrackerservice.kafka.PriceDropProducer;
import com.example.priceTrackerservice.repository.PriceAlertRepository;
import org.example.common.dto.PriceDropDetectedEvent;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PriceTrackerService {
    private final SearchClient searchClient;
    private final PriceDropProducer producer;
    private final PriceAlertRepository repo;

    public PriceTrackerService(SearchClient searchClient,
                               PriceDropProducer producer,
                               PriceAlertRepository repo) {
        this.searchClient = searchClient;
        this.producer = producer;
        this.repo = repo;
    }

    public void checkAllPrices() {

        List<PriceAlert> alerts = repo.findByActiveTrue();

        for (PriceAlert alert : alerts) {

            // 1️⃣ get price as BigDecimal
            BigDecimal livePrice = searchClient.getLivePrice(alert.getSearchKey());

            // 2️⃣ compare BigDecimal properly
            if (livePrice.compareTo(alert.getTargetPrice()) <= 0) {

                // 3️⃣ correct constructor (Long, Long, String, String, BigDecimal, BigDecimal)
                PriceDropDetectedEvent event = new PriceDropDetectedEvent(
                        alert.getId(),               // Long
                        alert.getUserId(),           // Long
                        alert.getUserEmail(),        // String
                        alert.getFlightId(),         // String
                        alert.getTargetPrice(),      // BigDecimal
                        livePrice                    // BigDecimal
                );

                producer.sendPriceDropEvent(event);

                alert.setActive(false);
                repo.save(alert);
            }
        }
    }
}
