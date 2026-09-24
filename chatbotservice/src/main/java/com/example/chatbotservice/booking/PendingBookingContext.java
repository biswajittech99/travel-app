package com.example.chatbotservice.booking;

import java.time.LocalDateTime;

public record PendingBookingContext(String conversationId,
                                    String userId,
                                    PendingBookingType type,
                                    String bookingData,
                                    String selectedData,
                                    BookingWorkflowState state,
                                    String paymentMethod,
                                    LocalDateTime createdAt) {

}
