package org.example.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelInventoryDTO {

    private String hotelId;

    private String supplier;

    private String hotelName;

    private String city;

    private String address;

    private Double latitude;

    private Double longitude;

    private BigDecimal price;

    private String currency;

    private Integer availableRooms;

    private List<String> amenities;

    private Double rating;

    private LocalDate checkIn;

    private LocalDate checkOut;

    private LocalDateTime lastSyncedAt;

}
