package com.example.priceTrackerservice.service;

import com.example.priceTrackerservice.client.SearchClient;
import com.example.priceTrackerservice.entity.PriceAlert;
import com.example.priceTrackerservice.repository.PriceAlertRepository;
import lombok.RequiredArgsConstructor;
import org.example.common.dto.PriceDropDetectedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PriceDropScheduler {
    private final PriceAlertRepository repo;
    private final KafkaTemplate<String, PriceDropDetectedEvent> kafkaTemplate;
    private final SearchClient searchClient;// Feign / RestTemplate

    @Scheduled(fixedDelay = 60000) // every 1 minute
    public void checkPriceDrops() {

        List<PriceAlert> alerts = repo.findByActiveTrue();

        for (PriceAlert alert : alerts) {   // ✅ ✅ NOW alert EXISTS

            BigDecimal livePrice = searchClient.getLivePrice(alert.getFlightId());

            if (livePrice.compareTo(alert.getTargetPrice()) <= 0) {

                PriceDropDetectedEvent event = new PriceDropDetectedEvent(
                        alert.getId(),
                        alert.getUserId(),
                        alert.getUserEmail(),
                        alert.getFlightId(),
                        alert.getTargetPrice(),
                        livePrice

                );

                kafkaTemplate.send("price.drop.detected", alert.getId().toString(), event);

                // ✅ Disable alert after trigger
                alert.setActive(false);
                repo.save(alert);
            }
        }
    }
}
