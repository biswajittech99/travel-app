package com.example.chatbotservice.rag.dto;

public record FlightSearchRequest( String origin,

                                   String destination,

                                   String departureDate) {
}
