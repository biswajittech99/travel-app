package com.example.chatbotservice.booking;

import java.time.LocalDateTime;

public record LiveSearchContext(String conversationId,

                                PendingBookingType type,

                                String searchData,

                                LocalDateTime createdAt) {
}
