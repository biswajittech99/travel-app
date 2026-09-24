package com.example.searchservice.service;

import com.example.searchservice.dto.HotelOption;
import org.example.common.dto.HotelInventoryDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class HotelInventoryPublisherService {

    private final HotelInventoryProducer producer;

    public HotelInventoryPublisherService(HotelInventoryProducer producer)
    {
        this.producer= producer;
    }

    public void publish(HotelOption option,
                        LocalDate checkIn,
                        LocalDate checkOut) {

        HotelInventoryDTO dto =
                new HotelInventoryDTO();

        dto.setHotelId(option.hotelId());

        dto.setSupplier(option.supplier());

        dto.setHotelName(option.hotelName());

        dto.setCity(option.city());

        dto.setAddress(option.address());

        dto.setLatitude(option.latitude());

        dto.setLongitude(option.longitude());

        dto.setPrice(option.price());

        dto.setCurrency(option.currency());

        dto.setAvailableRooms(option.availableRooms());

        dto.setAmenities(option.amenities());

        dto.setRating(option.rating());

        dto.setCheckIn(checkIn);

        dto.setCheckOut(checkOut);

        dto.setLastSyncedAt(LocalDateTime.now());

        producer.publish(dto);

    }


}
