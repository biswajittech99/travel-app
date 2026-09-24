package com.example.bookingservice.service;

public class InsufficientSeatsException extends RuntimeException{


    public InsufficientSeatsException(String notEnoughSeatsAvailable) {
    }
}
