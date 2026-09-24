package com.example.searchservice.service;

import org.example.common.dto.FlightInventoryDTO;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
public class InventoryProducer {
    private static final String FLIGHT_TOPIC =
            "flight-inventory";

    private final KafkaTemplate<String,Object> kafkaTemplate;

    public InventoryProducer(
            KafkaTemplate<String,Object> kafkaTemplate) {

        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishFlight(
            FlightInventoryDTO dto) {

        kafkaTemplate.send(
                FLIGHT_TOPIC,
                dto.getFlightId(),
                dto);
    }

}
