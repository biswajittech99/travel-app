package com.example.bookingservice.controller;

import com.example.bookingservice.entity.FlightInventory;
import com.example.bookingservice.repo.FlightInventoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final FlightInventoryRepository repo;

    public AdminController(FlightInventoryRepository repo) {
        this.repo = repo;
    }

    @PostMapping("/inventory")
    public ResponseEntity<FlightInventory> upsertInventory(@RequestBody InventoryRequest r) {

        // Optional<FlightInventory>
        Optional<FlightInventory> existingOpt = repo.findByFlightId(r.getFlightId());

        // unwrap Optional → get entity OR create new one
        FlightInventory existing = existingOpt.orElseGet(() -> {
            FlightInventory inv = new FlightInventory();
            inv.setFlightId(r.getFlightId());
            return inv;
        });

        // map fields
        existing.setSupplier(r.getSupplier());
        existing.setFromAirport(r.getFromAirport());
        existing.setToAirport(r.getToAirport());
        existing.setDeparture(r.getDeparture());
        existing.setArrival(r.getArrival());
        existing.setSeatsAvailable(r.getSeatsAvailable());
        existing.setPrice(BigDecimal.valueOf(r.getPrice()));
        existing.setCurrency(r.getCurrency());
        existing.setSupplierOfferId(r.getSupplierOfferId());
        existing.setLastSyncedAt(LocalDateTime.now());

        FlightInventory saved = repo.save(existing);
        return ResponseEntity.ok(saved);
    }

    // DTO for incoming JSON request
    public static class InventoryRequest {
        private String flightId;
        private String supplier;
        private String fromAirport;
        private String toAirport;
        private LocalDateTime departure;
        private LocalDateTime arrival;
        private int seatsAvailable;
        private double price;
        private String currency;
        private String supplierOfferId;

        // getters and setters
        public String getFlightId() { return flightId; }
        public void setFlightId(String flightId) { this.flightId = flightId; }
        public String getSupplier() { return supplier; }
        public void setSupplier(String supplier) { this.supplier = supplier; }
        public String getFromAirport() { return fromAirport; }
        public void setFromAirport(String fromAirport) { this.fromAirport = fromAirport; }
        public String getToAirport() { return toAirport; }
        public void setToAirport(String toAirport) { this.toAirport = toAirport; }
        public LocalDateTime getDeparture() { return departure; }
        public void setDeparture(LocalDateTime departure) { this.departure = departure; }
        public LocalDateTime getArrival() { return arrival; }
        public void setArrival(LocalDateTime arrival) { this.arrival = arrival; }
        public int getSeatsAvailable() { return seatsAvailable; }
        public void setSeatsAvailable(int seatsAvailable) { this.seatsAvailable = seatsAvailable; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public String getSupplierOfferId() { return supplierOfferId; }
        public void setSupplierOfferId(String supplierOfferId) { this.supplierOfferId = supplierOfferId; }
    }

}
