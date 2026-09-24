package com.example.chatbotservice.airport.service;

import com.example.chatbotservice.airport.dto.AirportDTO;

import java.util.Optional;

public interface AirportResolverService {

    Optional<AirportDTO> resolve(String input);
}
