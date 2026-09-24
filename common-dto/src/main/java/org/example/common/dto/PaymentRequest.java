package org.example.common.dto;

public record PaymentRequest(String bookingUid, String userId, double amount, String currency, String paymentMethod,String userEmail) {
}
