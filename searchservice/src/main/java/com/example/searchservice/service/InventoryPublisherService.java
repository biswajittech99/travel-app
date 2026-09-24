package com.example.searchservice.service;

import com.example.searchservice.dto.TravelOption;
import org.example.common.dto.FlightInventoryDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Service
public class InventoryPublisherService {


    private final InventoryProducer producer;

    public InventoryPublisherService(
            InventoryProducer producer) {

        this.producer = producer;
    }

    public void publishFlightInventory(
            TravelOption option) {

        FlightInventoryDTO dto =
                new FlightInventoryDTO();

        dto.setFlightId(
                option.flightNumber() != null
                        ? option.supplier() + ":" + option.flightNumber()
                        : option.supplier() + ":" + option.id());

        dto.setSupplier(option.supplier());

        dto.setSupplierOfferId(option.id());

        dto.setFromAirport(option.from());

        dto.setToAirport(option.to());

        dto.setDeparture(
                LocalDateTime.ofInstant(
                        option.departure(),
                        ZoneOffset.UTC));

        dto.setArrival(
                LocalDateTime.ofInstant(
                        option.arrival(),
                        ZoneOffset.UTC));

        dto.setSeatsAvailable(
                option.seatsAvailable());

        dto.setPrice(option.price());

        dto.setCurrency(
                Optional.ofNullable(option.currency())
                        .orElse("INR"));

        dto.setLastSyncedAt(
                LocalDateTime.now());

        producer.publishFlight(dto);
    }

}
