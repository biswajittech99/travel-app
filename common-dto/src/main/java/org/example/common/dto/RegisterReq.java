package org.example.common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterReq(@NotBlank(message = "name is required")
                          String name,
                          @Email(message = "Invalid email")
                          @NotBlank(message = "Email is required")
                          String email,

                          @Size(min = 6, message = "Password must be at least 6 characters")
                          @NotBlank(message = "Password is required")
                          String password) {
}
