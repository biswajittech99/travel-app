package com.example.chatbotservice.booking.service;

import com.example.chatbotservice.booking.BookingWorkflowState;
import com.example.chatbotservice.booking.PendingBookingType;

import java.time.LocalDateTime;

public record PendingBookingContex(String conversationId,

                                   String userId,

                                   PendingBookingType type,

                                   String selectedData,

                                   BookingWorkflowState state,

                                   LocalDateTime createdAt,

                                   LocalDateTime updatedAt) {
}
