package com.example.bookingservice.service;

public class SoldOutException extends RuntimeException{
    public SoldOutException(String message) {
        super(message);
    }
}
