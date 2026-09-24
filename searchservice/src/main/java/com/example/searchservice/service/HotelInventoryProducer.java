package com.example.searchservice.service;

import org.example.common.dto.HotelInventoryDTO;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class HotelInventoryProducer {

    private static final String TOPIC = "hotel-inventory";

    private final KafkaTemplate<String, HotelInventoryDTO> kafkaTemplate;

    public HotelInventoryProducer(
            KafkaTemplate<String, HotelInventoryDTO> kafkaTemplate) {

        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(HotelInventoryDTO dto) {

        kafkaTemplate.send(
                TOPIC,
                dto.getHotelId(),
                dto);

    }
}
