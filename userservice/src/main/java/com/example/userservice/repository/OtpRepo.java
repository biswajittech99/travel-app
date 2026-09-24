package com.example.userservice.repository;

import com.example.userservice.entity.EmailOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpRepo extends JpaRepository<EmailOtp,Long> {
    Optional<EmailOtp> findByEmail(String email);

    Optional<EmailOtp> findTopByEmailOrderByExpiryDesc(String email);

    void deleteByEmail(String email);
}

