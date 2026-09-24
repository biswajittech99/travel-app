package org.example.common.dto;

public record SendOtpEmailEvent(String email,
                                String otp) {
}
