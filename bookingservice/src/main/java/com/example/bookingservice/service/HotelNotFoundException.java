package com.example.bookingservice.service;

public class HotelNotFoundException extends  RuntimeException{

    public HotelNotFoundException(String message) {
        super(message);
    }
}
