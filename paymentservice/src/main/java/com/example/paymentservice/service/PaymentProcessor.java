package com.example.paymentservice.service;

import org.example.common.dto.PaymentRequest;
import org.example.common.dto.PaymentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PaymentProcessor {

    private final KafkaTemplate<String, PaymentResult> kafka;
    private final Logger log = LoggerFactory.getLogger(getClass());

    // ✅ Topics from config, not hardcoded
    @Value("${payment.topics.success:payment.succeeded}")
    private String successTopic;

    @Value("${payment.topics.failed:payment.failed}")
    private String failedTopic;

    public PaymentProcessor(KafkaTemplate<String, PaymentResult> kafka) {
        this.kafka = kafka;
    }

    /**
     * Called from REST /charge endpoint (manual/sync)
     * Returns result immediately + publishes to Kafka
     */
    public PaymentResult process(PaymentRequest req) {
        log.info("💳 Processing payment for booking: {} method: {}",
                req.bookingUid(), req.paymentMethod());

        PaymentResult result = buildResult(req);
        publish(result);
        return result;
    }

    /**
     * Called from PaymentRequestConsumer (Kafka async flow)
     * No return needed — result goes to Kafka
     */
    public void processAsync(PaymentRequest req) {
        log.info("📩 Async processing payment for booking: {}",
                req.bookingUid());
        PaymentResult result = buildResult(req);
        publish(result);
    }

    /**
     * Called from /webhook/simulate endpoint
     * Directly publishes a pre-built result
     * Simulates real payment gateway webhook
     */
    public void publishResult(PaymentResult res) {
        log.info("🔔 Manual publish for booking: {} success: {}",
                res.bookingUid(), res.success());
        publish(res);
    }

    // ─── Private Helpers ────────────────────────────────

    private PaymentResult buildResult(PaymentRequest req) {

        // ✅ Determine success
        boolean success = simulatePayment(req);

        // ✅ providerId only set on success
        String providerId = success
                ? "PAY-" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase()
                : null;   // ← null on failure, not fake ID

        // ✅ errorMessage only set on failure
        String errorMessage = success
                ? null
                : resolveErrorMessage(req.paymentMethod());

        return new PaymentResult(
                req.bookingUid(),
                req.userId(),
                success,
                providerId,
                req.amount(),
                req.currency(),
                req.paymentMethod(),
                req.userEmail(),
                errorMessage
        );
    }

    private void publish(PaymentResult result) {

        // ✅ Topic from config fields, not hardcoded strings
        String topic = result.success() ? successTopic : failedTopic;

        kafka.send(topic, result.bookingUid(), result)
                .whenComplete((res, ex) -> {
                    if (ex == null) {
                        log.info("✅ Published to [{}] for booking: {}",
                                topic, result.bookingUid());
                    } else {
                        log.error("❌ Kafka publish failed for booking: {} error: {}",
                                result.bookingUid(), ex.getMessage());
                        // Production: save to outbox table for retry
                    }
                });
    }

    /**
     * Payment simulation logic
     * Real app: call Razorpay / Stripe / PayU API here
     */
    private boolean simulatePayment(PaymentRequest req) {

        // ✅ Amount validation
        if (req.amount() <= 0) {
            log.warn("⚠️ Invalid amount {} for booking: {}",
                    req.amount(), req.bookingUid());
            return false;
        }

        // ✅ Test hooks for demo
        return switch (req.paymentMethod().toUpperCase()) {
            case "FAIL"    -> false;   // force failure
            case "TIMEOUT" -> {
                simulateDelay(3000);   // test slow payment
                yield true;
            }
            default        -> true;    // UPI, CARD, NETBANKING etc → success
        };
    }

    /**
     * Meaningful error messages per failure type
     */
    private String resolveErrorMessage(String paymentMethod) {
        return switch (paymentMethod.toUpperCase()) {
            case "FAIL"    -> "Simulated payment failure";
            case "TIMEOUT" -> "Payment gateway timeout";
            default        -> "Payment declined by gateway";
        };
    }

    private void simulateDelay(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
