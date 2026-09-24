package com.example.searchservice.service;

import com.example.searchservice.dto.HotelOption;
import com.example.searchservice.enums.TravelType;
import com.example.searchservice.request.TravelSearchRequest;
import com.example.searchservice.supplier.SerpApiHotelClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.common.dto.HotelInventoryDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@EnableScheduling
public class HotelInventorySyncService {

    private static final Logger log =
            LoggerFactory.getLogger(HotelInventorySyncService.class);

    private final StringRedisTemplate redis;

    private final ObjectMapper mapper;

    private final SerpApiHotelClient hotelClient;

    private final HotelInventoryProducer producer;

    public HotelInventorySyncService(
            StringRedisTemplate redis,
            ObjectMapper mapper,
            SerpApiHotelClient hotelClient,
            HotelInventoryProducer producer) {

        this.redis = redis;
        this.mapper = mapper;
        this.hotelClient = hotelClient;
        this.producer = producer;
    }

    @Scheduled(fixedDelay = 60000)
    public void syncHotels() {

        log.info("Starting Hotel Inventory Sync...");

        Set<String> hotelIds =
                redis.opsForSet()
                        .members("inventory:hotelIds");

        if (hotelIds == null || hotelIds.isEmpty()) {

            log.info("No hotels available for sync.");
            return;
        }

        for (String hotelId : hotelIds) {

            try {

                String key = "inventory:hotel:" + hotelId;

                String json =
                        redis.opsForValue().get(key);

                if (json == null) {
                    continue;
                }

                HotelInventoryDTO oldInventory =
                        mapper.readValue(
                                json,
                                HotelInventoryDTO.class);

                /*
                 * Skip expired inventory.
                 */

                if (oldInventory.getCheckOut()
                        .isBefore(LocalDate.now())) {

                    log.info(
                            "Removing expired hotel inventory : {}",
                            hotelId);

                    redis.opsForSet()
                            .remove("inventory:hotelIds",
                                    hotelId);

                    redis.delete(key);

                    continue;
                }

                /*
                 * Build fresh search request.
                 */

                TravelSearchRequest request =
                        new TravelSearchRequest(

                                TravelType.HOTEL,

                                oldInventory.getCity(),

                                oldInventory.getCity(),

                                oldInventory.getCheckIn(),

                                oldInventory.getCheckOut(),

                                2,

                                0,

                                1
                        );

                List<HotelOption> latestHotels =
                        hotelClient.searchHotels(request)
                                .block();

                if (latestHotels == null) {
                    continue;
                }

                latestHotels.stream()

                        .filter(h ->
                                h.hotelId()
                                        .equals(oldInventory.getHotelId()))

                        .findFirst()

                        .ifPresent(hotel -> {

                            try {

                                HotelInventoryDTO dto =
                                        convert(
                                                hotel,
                                                oldInventory.getCheckIn(),
                                                oldInventory.getCheckOut());

                                dto.setLastSyncedAt(
                                        LocalDateTime.now());

                                producer.publish(dto);

                                redis.opsForValue().set(
                                        key,
                                        mapper.writeValueAsString(dto));

                                log.info(
                                        "Hotel Synced : {}",
                                        dto.getHotelId());

                            } catch (Exception ex) {

                                log.error(
                                        "Hotel Sync Failed : {}",
                                        ex.getMessage());

                            }

                        });

            } catch (Exception ex) {

                log.error(
                        "Hotel Sync Error : {}",
                        ex.getMessage());

            }

        }

    }
    private HotelInventoryDTO convert(
            HotelOption hotel,
            LocalDate checkIn,
            LocalDate checkOut) {

        HotelInventoryDTO dto =
                new HotelInventoryDTO();

        dto.setHotelId(hotel.hotelId());

        dto.setSupplier(hotel.supplier());

        dto.setHotelName(hotel.hotelName());

        dto.setCity(hotel.city());

        dto.setAddress(hotel.address());

        dto.setLatitude(hotel.latitude());

        dto.setLongitude(hotel.longitude());

        dto.setPrice(hotel.price());

        dto.setCurrency(hotel.currency());

        dto.setAvailableRooms(
                hotel.availableRooms());

        dto.setAmenities(
                hotel.amenities());

        dto.setRating(
                hotel.rating());

        dto.setCheckIn(checkIn);

        dto.setCheckOut(checkOut);

        dto.setLastSyncedAt(
                LocalDateTime.now());

        return dto;
    }

}
