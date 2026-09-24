package com.example.chatbotservice.airport.repository;

import com.example.chatbotservice.airport.entity.Airport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AirportRepository extends JpaRepository<Airport,Long> {

    Optional<Airport> findByIataCodeIgnoreCase(
            String iataCode);

    Optional<Airport> findFirstByCityIgnoreCase(
            String city);

    Optional<Airport> findFirstByAirportNameContainingIgnoreCase(
            String airportName);

    List<Airport> findTop10ByCityContainingIgnoreCase(
            String city);
}


