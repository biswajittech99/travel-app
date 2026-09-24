package com.example.chatbotservice.booking;

public enum BookingWorkflowState {

    SELECTED,

    CONFIRMATION_RECEIVED,

    REVALIDATING,

    PRICE_CHANGED,

    READY_FOR_BOOKING,

    AWAITING_PAYMENT_METHOD,

    BOOKING_CREATED,

    PAYMENT_PENDING,

    CONFIRMED,

    FAILED
}
