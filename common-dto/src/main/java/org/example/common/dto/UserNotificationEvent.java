package org.example.common.dto;

public record UserNotificationEvent(String email,
                                    String subject,
                                    String body) {
}
