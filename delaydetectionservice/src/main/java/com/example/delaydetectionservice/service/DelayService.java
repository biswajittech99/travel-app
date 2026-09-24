package com.example.delaydetectionservice.service;


import com.example.delaydetectionservice.client.BookingClient;
import com.example.delaydetectionservice.entity.FlightStatus;
import com.example.delaydetectionservice.repository.FlightStatusRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.common.dto.BookingInfo;
import org.example.common.dto.DelayDetectedEvent;
import org.example.common.dto.FlightStatusNormalizedEvent;
import org.example.common.dto.FlightStatusUpdateDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DelayService {

    private static final int DELAY_THRESHOLD = 60;

    private final FlightStatusRepository repo;
    private final BookingClient bookingClient;
    private final KafkaTemplate<String, DelayDetectedEvent> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final Logger log = LoggerFactory.getLogger(getClass());

    public void handleRawUpdate(FlightStatusUpdateDTO update) {

        // 1️⃣ Load previous status
        FlightStatus existing = repo.findById(update.flightId()).orElse(null);
        int previousDelay = existing != null && existing.getDelayMinutes() != null
                ? existing.getDelayMinutes()
                : 0;

        // 2️⃣ Normalize & persist
        FlightStatus status = new FlightStatus();
        status.setFlightId(update.flightId());
        status.setSupplier(update.supplier());
        status.setStatus(update.status());
        status.setDelayMinutes(update.delayMinutes());
        status.setStatusAt(update.updatedAt());
        status.setLastUpdatedAt(Instant.now());

        try {
            status.setRawPayload(objectMapper.writeValueAsString(update));
        } catch (Exception e) {
            status.setRawPayload("{}");
        }

        repo.save(status);

        // 3️⃣ Detect threshold crossing
        if (previousDelay < DELAY_THRESHOLD &&
                update.delayMinutes() >= DELAY_THRESHOLD) {

            publishDelayEvents(update);
        }
    }

    private void publishDelayEvents(FlightStatusUpdateDTO update) {

        List<BookingInfo> bookings =List.of();
        try {
            bookings = bookingClient.findBookingsByFlight(update.flightId());
        }catch(Exception ex){
            log.warn("Booking Service Unavailable publish to delay without bookings");
        }

        for (BookingInfo booking : bookings) {

            DelayDetectedEvent event =
                    new DelayDetectedEvent(
                            update.flightId(),
                            booking.bookingUid(),
                            booking.userId(),
                            booking.userEmail(),
                            update.delayMinutes(),
                            Instant.now()
                    );

            kafkaTemplate.send(
                    "flight.delay.detected",
                    booking.bookingUid(),
                    event
            );
        }
    }
}