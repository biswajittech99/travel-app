package com.example.chatbotservice.airport.service.impl;

import com.example.chatbotservice.airport.dto.AirportDTO;
import com.example.chatbotservice.airport.entity.Airport;
import com.example.chatbotservice.airport.repository.AirportRepository;
import com.example.chatbotservice.airport.service.AirportResolverService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class AirportResolverServiceImpl implements AirportResolverService {

    private final AirportRepository airportRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public AirportResolverServiceImpl(
            AirportRepository airportRepository, StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {

        this.airportRepository = airportRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }


    @Override
    public Optional<AirportDTO> resolve(String input) {

        if (input == null || input.isBlank()) {
            return Optional.empty();
        }

        String keyword = input.trim().toUpperCase();
        String redisKey = "airport:" + keyword;

        // ==========================
        // 1. Try Redis first
        // ==========================
        String cached = redisTemplate.opsForValue().get(redisKey);

        if (cached != null) {
            try {
                AirportDTO dto =
                        objectMapper.readValue(cached, AirportDTO.class);

                return Optional.of(dto);

            } catch (Exception e) {
                // Corrupted cache entry
                redisTemplate.delete(redisKey);
            }
        }

        // ==========================
        // 2. Query MySQL
        // ==========================
        Optional<Airport> airport =
                airportRepository.findByIataCodeIgnoreCase(keyword);

        if (airport.isEmpty()) {
            airport =
                    airportRepository.findFirstByCityIgnoreCase(keyword);
        }

        if (airport.isEmpty()) {
            airport =
                    airportRepository
                            .findFirstByAirportNameContainingIgnoreCase(keyword);
        }

        // ==========================
        // 3. Cache + Return
        // ==========================
        if (airport.isPresent()) {

            Airport a = airport.get();

            AirportDTO dto = new AirportDTO(

                    a.getIataCode(),

                    a.getCity(),

                    a.getAirportName(),

                    a.getCountry()
            );

            try {

                redisTemplate.opsForValue().set(

                        redisKey,

                        objectMapper.writeValueAsString(dto),

                        Duration.ofDays(30)

                );

            } catch (Exception e) {

                // Cache failure should never fail the request
            }

            return Optional.of(dto);
        }

        return Optional.empty();
    }



    }
