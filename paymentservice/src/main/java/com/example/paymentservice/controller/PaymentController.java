package com.example.paymentservice.controller;

import com.example.paymentservice.service.PaymentProcessor;
import org.example.common.dto.PaymentRequest;
import org.example.common.dto.PaymentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentProcessor processor;
    private final Logger log = LoggerFactory.getLogger(getClass());

    public PaymentController(PaymentProcessor processor) {
        this.processor = processor;
    }

    /**
     * Sync charge — for direct API calls
     * Used by: manual testing, admin tools
     */
    @PostMapping("/charge")
    public ResponseEntity<PaymentResult> charge(
            @RequestBody PaymentRequest req) {
        log.info("🔌 Sync charge request for booking: {}",
                req.bookingUid());
        PaymentResult result = processor.process(req);
        return ResponseEntity.ok(result);
    }

    /**
     * Simulate webhook — for demo/testing only
     * Simulates payment gateway callback
     * Real app: Razorpay/Stripe sends this automatically
     */
    @PostMapping("/webhook/simulate")
    public ResponseEntity<String> simulateWebhook(
            @RequestBody PaymentResult result) {
        log.info("🔔 Webhook simulation for booking: {}",
                result.bookingUid());
        processor.publishResult(result);
        return ResponseEntity.ok("Result published to Kafka: "
                + (result.success() ? "payment.succeeded"
                : "payment.failed"));
    }

    /**
     * Health check — for demo
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Payment service running ✅");
    }
}
