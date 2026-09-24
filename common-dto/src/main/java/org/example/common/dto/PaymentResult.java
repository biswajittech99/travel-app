package org.example.common.dto;

public record PaymentResult( String bookingUid,
                             String userId,
                             boolean success,
                             String providerPaymentId,
                             double amount,
                             String currency,
                             String paymentMethod,
                             String userEmail,
                             String errorMessage) {
}
