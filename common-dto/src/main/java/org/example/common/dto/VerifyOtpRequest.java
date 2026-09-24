package org.example.common.dto;

public record VerifyOtpRequest(String email,
                               String otp) {
}
