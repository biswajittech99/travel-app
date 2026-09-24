//package com.example.searchservice.service;
//
//
//import com.example.searchservice.dto.TravelOption;
//import com.example.searchservice.enums.TravelType;
//import com.example.searchservice.request.TravelSearchRequest;
//import com.example.searchservice.supplier.SupplierClient;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.example.common.dto.FlightInventoryDTO;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.scheduling.annotation.EnableScheduling;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.time.ZoneOffset;
//import java.util.List;
//import java.util.Set;
//import java.util.stream.Stream;
//
//@Service
//@EnableScheduling
//public class TravelInventorySyncService {
//    private final StringRedisTemplate redis;
//    private final ObjectMapper mapper;
//    private final List<SupplierClient> suppliers;
//    private final InventoryProducer producer;
//    private final Logger log =
//            LoggerFactory.getLogger(TravelInventorySyncService.class);
//
//    public TravelInventorySyncService(
//            StringRedisTemplate redis,
//            List<SupplierClient> suppliers,
//            InventoryProducer producer,
//            ObjectMapper mapper) {     // ✅ inject shared mapper
//        this.redis = redis;
//        this.suppliers = suppliers;
//        this.producer = producer;
//        this.mapper = mapper;
//    }
//
//    @Scheduled(fixedDelay = 60_000)
//    public void syncAll() {
//        log.info("🔄 Starting inventory sync...");
//
//        Set<String> flightIds = redis.opsForSet()
//                .members("inventory:flightIds");
//
//        if (flightIds == null || flightIds.isEmpty()) {
//            log.info("No flight IDs to sync");
//            return;
//        }
//
//        for (String flightId : flightIds) {
//            try {
//                String key = "inventory:flight:" + flightId;
//                String json = redis.opsForValue().get(key);
//                if (json == null) continue;
//
//                FlightInventoryDTO old = mapper.readValue(
//                        json, FlightInventoryDTO.class);
//
//                // ✅ Skip expired flights
//                if (old.getDeparture().isBefore(LocalDateTime.now())) {
//                    log.info("🛑 Skipping expired: {}", flightId);
//                    redis.opsForSet()
//                            .remove("inventory:flightIds", flightId);
//                    continue;
//                }
//                TravelSearchRequest request =
//                        new TravelSearchRequest(
//
//                                TravelType.FLIGHT,
//                                old.getFromAirport(),
//                                old.getToAirport(),
//                                old.getDeparture().toLocalDate(),
//                                null,
//                                1,
//                                0,
//                                null
//
//                        );
//
//                suppliers.stream()
//                        .flatMap(s -> {
//                            try {
//                                return s.search(request)
//                                        .map(List::stream)
//                                        .blockOptional()
//                                        .orElseGet(() ->
//                                                List.<TravelOption>of().stream());
//                            } catch (Exception e) {
//                                log.warn("Supplier error: {}", e.getMessage());
//                                return Stream.<TravelOption>empty();
//                            }
//                        })
//                        .filter(t ->
//                                old.getSupplierOfferId() != null &&
//                                        old.getSupplierOfferId().equals(t.id()))
//                        .findFirst()
//                        .ifPresent(matching -> {
//                            try {
//                                FlightInventoryDTO dto = convert(matching);
//                                dto.setLastSyncedAt(LocalDateTime.now());
//                                producer.publishFlight(dto);
//                                redis.opsForValue().set(
//                                        key, mapper.writeValueAsString(dto));
//                                log.info("✅ Synced: {}", flightId);
//                            } catch (Exception e) {
//                                log.error("Sync save failed: {}",
//                                        e.getMessage());
//                            }
//                        });
//
//            } catch (Exception ex) {
//                log.error("❌ Sync failed for {}: {}",
//                        flightId, ex.getMessage());
//            }
//        }
//    }
//
//    private FlightInventoryDTO convert(TravelOption opt) {
//        FlightInventoryDTO dto = new FlightInventoryDTO();
//        dto.setFlightId(opt.supplier() + ":" + opt.id());
//        dto.setSupplier(opt.supplier());
//        dto.setFromAirport(opt.from());
//        dto.setToAirport(opt.to());
//        dto.setDeparture(LocalDateTime.ofInstant(
//                opt.departure(), ZoneOffset.UTC));
//        dto.setArrival(LocalDateTime.ofInstant(
//                opt.arrival(), ZoneOffset.UTC));
//        dto.setSeatsAvailable(opt.seatsAvailable());
//        dto.setPrice(opt.price());
//        dto.setCurrency("INR");
//        dto.setSupplierOfferId(opt.id());
//        dto.setLastSyncedAt(LocalDateTime.now());
//        return dto;
//    }
//
//}
