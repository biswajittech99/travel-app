package com.example.bookingservice.service;

public class RateLimitExceededException  extends RuntimeException{
    public RateLimitExceededException(String message) {
        super(message);
    }
}
