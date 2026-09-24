package com.example.bookingservice.service;

import com.example.bookingservice.entity.HotelInventory;
import com.example.bookingservice.repo.HotelInventoryRepository;
import org.example.common.dto.HotelInventoryDTO;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class HotelInventoryConsumer {

    private final HotelInventoryRepository repo;

    public HotelInventoryConsumer(HotelInventoryRepository repo) {
        this.repo = repo;
    }

    @KafkaListener(
            topics = "hotel-inventory",
            groupId = "booking-service-group"
    )
    public void consume(HotelInventoryDTO dto) {
        try {
            Optional<HotelInventory> optional = repo.findByHotelId(dto.getHotelId());

            HotelInventory existing = optional.orElseGet(() -> {
                HotelInventory h = new HotelInventory();
                h.setHotelId(dto.getHotelId());
                return h;
            });

            existing.setSupplier(dto.getSupplier());
            existing.setHotelName(dto.getHotelName());
            existing.setCity(dto.getCity());
            existing.setRoomsAvailable(dto.getAvailableRooms());
            existing.setPrice(dto.getPrice());
            existing.setCurrency(dto.getCurrency());
            existing.setCheckIn(dto.getCheckIn());
            existing.setCheckOut(dto.getCheckOut());
            existing.setLastSyncedAt(LocalDateTime.now());

            repo.save(existing);
            System.out.println("✅ Saved hotel: " + dto.getHotelId());

        } catch (Exception e) {
            System.err.println("❌ Failed to process hotel inventory message for DTO: " + dto);
            e.printStackTrace();
        }
    }
}
