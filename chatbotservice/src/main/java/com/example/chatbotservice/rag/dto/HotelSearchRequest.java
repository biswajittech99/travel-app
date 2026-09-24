package com.example.chatbotservice.rag.dto;

public record HotelSearchRequest( String city,

                                  String checkIn,

                                  String checkOut,

                                  Integer adults) {
}
