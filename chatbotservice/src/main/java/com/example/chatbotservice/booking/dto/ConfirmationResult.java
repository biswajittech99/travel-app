package com.example.chatbotservice.booking.dto;

import java.math.BigDecimal;

public record ConfirmationResult(
        boolean success,

        boolean priceChanged,

        boolean readyForBooking,

        BigDecimal oldPrice,

        BigDecimal newPrice,

        String currency,

        String message) {
    public static ConfirmationResult failure(
            String message
    ) {

        return new ConfirmationResult(
                false,
                false,
                false,
                null,
                null,
                null,
                message
        );
    }

    public static ConfirmationResult priceChanged(
            BigDecimal oldPrice,
            BigDecimal newPrice,
            String currency,
            String message
    ) {

        return new ConfirmationResult(
                true,
                true,
                false,
                oldPrice,
                newPrice,
                currency,
                message
        );
    }

    public static ConfirmationResult readyForBooking(
            String message
    ) {

        return new ConfirmationResult(
                true,
                false,
                true,
                null,
                null,
                null,
                message
        );
    }
}
