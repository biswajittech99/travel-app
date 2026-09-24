package com.example.userservice.service;

import com.example.userservice.entity.EmailOtp;
import com.example.userservice.entity.OutboxEvent;
import com.example.userservice.entity.User;
import com.example.userservice.repository.OtpRepo;
import com.example.userservice.repository.OutboxRepo;
import com.example.userservice.repository.UserRepository;
import org.example.common.dto.RegisterReq;
import org.example.common.dto.RegistrationSuccessEvent;
import org.example.common.dto.SendOtpEmailEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

@Service
public class OtpService {
    private final OtpRepo otpRepo;
    private final UserRepository userRepo;
    private final OutboxRepo outboxRepo;   // ✅ ADD THIS

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OtpService(OtpRepo otpRepo,
                      UserRepository userRepo,
                      KafkaTemplate<String, Object> kafkaTemplate,
                      OutboxRepo outboxRepo ) {
        this.otpRepo = otpRepo;
        this.userRepo = userRepo;
        this.kafkaTemplate = kafkaTemplate;
        this.outboxRepo = outboxRepo;

    }

    // ✅ SEND OTP
    public void sendOtp(RegisterReq req) {

        String otp = String.valueOf(new Random().nextInt(900000) + 100000);

        EmailOtp e = new EmailOtp();
        e.setName(req.name());
        e.setEmail(req.email());
        e.setPassword(new BCryptPasswordEncoder().encode(req.password()));
        e.setOtp(otp);
        e.setExpiry(LocalDateTime.now().plusMinutes(10));

        otpRepo.save(e);

        // ✅ Truly fire-and-forget on a separate thread
        CompletableFuture.runAsync(() -> {
            try {
                kafkaTemplate.send("send-otp-email-topic", new SendOtpEmailEvent(req.email(), otp));
            } catch (Exception ex) {
                System.out.println("Kafka send failed (non-blocking): " + ex.getMessage());
            }
        });
    }

    // ✅ VERIFY OTP
    @Transactional
    public void verifyOtp(String email, String otp) {

        EmailOtp savedOtp = otpRepo
                .findTopByEmailOrderByExpiryDesc(email)
                .orElseThrow(() ->
                        new RuntimeException("No OTP found for this email"));

        // ⛔ Expiry check
        if (savedOtp.getExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }

        // ⛔ Wrong OTP check
        if (!savedOtp.getOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        // ✅ Create user
        User user = new User();
        user.setName(savedOtp.getName());
        user.setEmail(savedOtp.getEmail());
        user.setPassword(savedOtp.getPassword());
        user.setEmailVerified(true);

        userRepo.save(user);

        // 🧹 delete OTP
        otpRepo.deleteByEmail(email);

        // ✅ SAVE EVENT (instead of Kafka)
        OutboxEvent event = new OutboxEvent();
        event.setEventType("USER_REGISTERED");
        event.setPayload(user.getEmail()); // or JSON
        event.setStatus("PENDING");

        outboxRepo.save(event);
    }
}
