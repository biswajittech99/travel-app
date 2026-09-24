package com.example.bookingservice.service;

import com.example.bookingservice.entity.FlightInventory;
import com.example.bookingservice.repo.BookingRepository;
import com.example.bookingservice.repo.FlightInventoryRepository;
import org.example.common.dto.BookingConfirmedEvent;
import org.example.common.dto.PaymentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PaymentSagaListener {
    private final BookingService bookingService;
    private final HotelBookingService hotelBookingService;
    private final BookingRepository flightBookingRepo;
    private final Logger log = LoggerFactory.getLogger(getClass());

    public PaymentSagaListener(
            BookingService bookingService,
            HotelBookingService hotelBookingService,
            BookingRepository flightBookingRepo) {
        this.bookingService = bookingService;
        this.hotelBookingService = hotelBookingService;
        this.flightBookingRepo = flightBookingRepo;
    }

    @KafkaListener(topics = "payment.succeeded", groupId = "booking-saga-group")
    public void onPaymentSucceeded(PaymentResult res) {
        log.info("📩 payment.succeeded received for booking {}", res.bookingUid());
        try {
            if (flightBookingRepo.findByBookingUid(res.bookingUid()).isPresent()) {
                bookingService.confirmBookingAfterPayment(res);
            } else {
                hotelBookingService.confirmBookingAfterPayment(res);
            }
        } catch (Exception e) {
            log.error("❌ Failed to confirm booking {} after payment success",
                    res.bookingUid(), e);
            // Kafka listener throws -> message will be redelivered per your
            // container error-handling config (add a DLQ here in production).
            throw e;
        }
    }

    @KafkaListener(topics = "payment.failed", groupId = "booking-saga-group")
    public void onPaymentFailed(PaymentResult res) {
        log.info("📩 payment.failed received for booking {}: {}",
                res.bookingUid(), res.errorMessage());
        try {
            if (flightBookingRepo.findByBookingUid(res.bookingUid()).isPresent()) {
                bookingService.handlePaymentFailure(res);
            } else {
                hotelBookingService.handlePaymentFailure(res);
            }
        } catch (Exception e) {
            log.error("❌ Failed to mark booking {} as payment-failed",
                    res.bookingUid(), e);
            throw e;
        }
    }




    private String generatePnr() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
