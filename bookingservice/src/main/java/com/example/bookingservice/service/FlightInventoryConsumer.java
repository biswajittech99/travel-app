package com.example.bookingservice.service;

import com.example.bookingservice.entity.FlightInventory;
import com.example.bookingservice.repo.FlightInventoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.common.dto.FlightInventoryDTO;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class FlightInventoryConsumer {
    private final FlightInventoryRepository repo;

    public FlightInventoryConsumer(FlightInventoryRepository repo) {
        this.repo = repo;
    }

    @KafkaListener(
            topics = "flight-inventory",
            groupId = "booking-service-group"
    )
    public void consume(FlightInventoryDTO dto) {
        try {
            LocalDate departureDate = dto.getDeparture().toLocalDate();
            LocalDate today = LocalDate.now();

            // ✅ Skip expired flights
            if (departureDate.isBefore(today)) {
                System.out.println("🛑 Skipping expired flight: " + dto.getFlightId());
                return;
            }

            // ✅ Upsert (insert or update existing)
            Optional<FlightInventory> optional = repo.findByFlightId(dto.getFlightId());

            FlightInventory existing = optional.orElseGet(() -> {
                FlightInventory f = new FlightInventory();
                f.setFlightId(dto.getFlightId());
                return f;
            });

            existing.setSupplier(dto.getSupplier());
            existing.setFromAirport(dto.getFromAirport());
            existing.setToAirport(dto.getToAirport());
            existing.setDeparture(dto.getDeparture());
            existing.setArrival(dto.getArrival());
            existing.setSeatsAvailable(dto.getSeatsAvailable());
            existing.setPrice(dto.getPrice());
            existing.setCurrency(dto.getCurrency());
            existing.setSupplierOfferId(dto.getSupplierOfferId());
            existing.setLastSyncedAt(LocalDateTime.now());

            repo.save(existing);
            System.out.println("✅ Saved flight: " + dto.getFlightId());

        } catch (Exception e) {
            System.err.println("❌ Failed to process flight inventory message for DTO: " + dto);
            e.printStackTrace();
        }
    }
}