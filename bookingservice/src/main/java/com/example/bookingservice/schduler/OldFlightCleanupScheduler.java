package com.example.bookingservice.schduler;

import com.example.bookingservice.entity.FlightInventory;
import com.example.bookingservice.repo.FlightInventoryRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class OldFlightCleanupScheduler {
    private final FlightInventoryRepository flightRepo;

    public OldFlightCleanupScheduler(FlightInventoryRepository flightRepo) {
        this.flightRepo = flightRepo;
    }

    @Scheduled(fixedRate = 60000) // run every 1 minute
    @Transactional
    public void cleanupOldFlights() {
        LocalDateTime now = LocalDateTime.now();

        // Fetch all old and still active flights
        List<FlightInventory> oldFlights = flightRepo.findByDepartureBeforeAndActiveTrue(now);

        // Mark them inactive instead of deleting
        for (FlightInventory flight : oldFlights) {
            flight.setActive(false);
        }

        if (!oldFlights.isEmpty()) {
            flightRepo.saveAll(oldFlights);
            System.out.println("Soft-deleted " + oldFlights.size() + " expired flights at " + now);
        }
    }
}
