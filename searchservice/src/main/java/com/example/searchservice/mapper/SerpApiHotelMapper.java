package com.example.searchservice.mapper;

import com.example.searchservice.dto.HotelOption;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class SerpApiHotelMapper {

    public List<HotelOption> map(
            JsonNode root,
            String city) {

        List<HotelOption> hotels =
                new ArrayList<>();

        JsonNode properties =
                root.path("properties");

        if (!properties.isArray()) {
            return hotels;
        }

        for (JsonNode hotel : properties) {

            // ==============================
            // HOTEL ID
            // ==============================

            String hotelId =
                    hotel.path("property_token")
                            .asText(null);


            // ==============================
            // HOTEL NAME
            // ==============================

            String hotelName =
                    hotel.path("name")
                            .asText(null);


            // ==============================
            // HOTEL ADDRESS
            // ==============================

            String address =
                    hotel.path("address")
                            .asText(null);


            // ==============================
            // GPS LOCATION
            // ==============================

            JsonNode gps =
                    hotel.path("gps_coordinates");

            Double latitude =
                    gps.path("latitude").isNumber()
                            ? gps.path("latitude").asDouble()
                            : null;

            Double longitude =
                    gps.path("longitude").isNumber()
                            ? gps.path("longitude").asDouble()
                            : null;


            // ==============================
            // TOTAL PRICE
            // ==============================

            BigDecimal price = null;

            JsonNode totalRate =
                    hotel.path("total_rate");

            if (totalRate
                    .path("extracted_lowest")
                    .isNumber()) {

                price = BigDecimal.valueOf(
                        totalRate
                                .path("extracted_lowest")
                                .asDouble()
                );
            }


            // ==============================
            // RATING
            // ==============================

            Double rating = null;

            if (hotel
                    .path("overall_rating")
                    .isNumber()) {

                rating =
                        hotel.path("overall_rating")
                                .asDouble();
            }

            // ==============================
            // HOTEL CLASS / TYPE
            // ==============================

            String hotelType =
                    hotel.path("type")
                            .asText(null);


            // ==============================
            // CREATE HOTEL OPTION
            // ==============================

            hotels.add(
                    new HotelOption(

                            "SerpAPI",

                            hotelId,

                            hotelName,

                            city,

                            address,

                            latitude,

                            longitude,

                            price,

                            "INR",

                            0,

                            List.of(),

                            List.of(),

                            rating
                    )
            );
        }

        return hotels;
    }

        }

