package com.example.userservice.controller;

import com.example.userservice.entity.User;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.AuthService;
import com.example.userservice.service.OtpService;
import jakarta.validation.Valid;
import org.example.common.dto.LoginReq;
import org.example.common.dto.RegisterReq;
import org.example.common.dto.UserNotificationEvent;
import org.example.common.dto.VerifyOtpRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final OtpService otpService;
    private final AuthService authService;

    @Value("${app.topics.user-notification}")
    private String userNotificationTopic;

    public UserController(UserRepository userRepository, KafkaTemplate<String, Object> kafkaTemplate,
             OtpService otpService, AuthService authService) {
        this.userRepository = userRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.otpService = otpService;
        this.authService = authService;
    }

    @PostMapping("/auth/register")
    public String register(@Valid @RequestBody RegisterReq req) {
        // Block if user already fully registered
        if (userRepository.findByEmail(req.email()).isPresent()) {
            throw new RuntimeException("Account already exists. Please login.");
        }

        otpService.sendOtp(req);

        return "OTP sent to email";

    }

    @PostMapping("/auth/verify-otp")
    public String verifyOtp(@RequestBody VerifyOtpRequest request) {
        otpService.verifyOtp(request.email(), request.otp());
        return "Email verified successfully";
    }

    @PostMapping("/auth/login")
    public Map<String, String> login(@RequestBody LoginReq request) {
        String token = authService.login(request.email(), request.password());
        return Map.of("token", token);
    }

    @PostMapping
    public String createUser(@RequestBody User userRequest) {
        // ✅ Check if email already exists
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            return "❌ User already exists! One email ID per user.";
        }

        // ✅ Save new user
        User savedUser = userRepository.save(userRequest);

        // ✅ Send welcome email event
        UserNotificationEvent welcomeEvent = new UserNotificationEvent(
                savedUser.getEmail(),
                "Welcome to TravelApp!",
                "Hi " + savedUser.getName() + ",\n\nWelcome to TravelApp! We're excited to have you onboard.\n\nBest,\nTravelApp Team"
        );

        kafkaTemplate.send(userNotificationTopic, savedUser.getId(), welcomeEvent);

        return "✅ User created successfully. Welcome email sent to " + savedUser.getEmail();
    }




    @GetMapping("/{id}")
    public ResponseEntity<User> get(@PathVariable String id) {
        return userRepository.findById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<User> list() {
        return userRepository.findAll();
    }
}
