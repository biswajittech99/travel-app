package com.example.bookingservice.controller;

import com.example.bookingservice.entity.HotelBooking;
import com.example.bookingservice.repo.HotelBookingRepository;
import com.example.bookingservice.service.HotelBookingService;
import com.example.bookingservice.service.HotelNotFoundException;
import com.example.bookingservice.service.InsufficientSeatsException;
import com.example.bookingservice.service.RateLimitExceededException;
import org.example.common.dto.CreateHotelBookingRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings/hotels")
public class HotelBookingController {

    private final HotelBookingService hotelBookingService;
    private final HotelBookingRepository hotelBookingRepo;

    public HotelBookingController(
            HotelBookingService hotelBookingService,
            HotelBookingRepository hotelBookingRepo) {
        this.hotelBookingService = hotelBookingService;
        this.hotelBookingRepo = hotelBookingRepo;
    }

    @PostMapping
    public ResponseEntity<?> bookHotelFromChat(
            @RequestBody CreateHotelBookingRequest req,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        try {
            HotelBooking booking = hotelBookingService.createBooking(req, idempotencyKey);
            return ResponseEntity.ok(booking);
        } catch (HotelNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (InsufficientSeatsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (RateLimitExceededException e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{bookingUid}")
    public ResponseEntity<?> getBooking(@PathVariable String bookingUid) {
        return hotelBookingRepo.findByBookingUid(bookingUid)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
