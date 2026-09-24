package com.example.bookingservice.controller;

import com.example.bookingservice.dto.BookingRequest;
import com.example.bookingservice.dto.BookingResponse;
import com.example.bookingservice.dto.PriceSnapshot;
import com.example.bookingservice.entity.Booking;
import com.example.bookingservice.repo.BookingRepository;
import com.example.bookingservice.service.BookingService;
import com.example.bookingservice.service.FlightNotFoundException;
import com.example.bookingservice.service.RateLimitExceededException;
import com.example.bookingservice.service.SoldOutException;
import org.example.common.dto.BookingInfo;
import org.example.common.dto.CreateFlightBookingRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@Validated
public class BookingController {

    private final BookingService bookingService;
    private final BookingRepository bookingRepo;

    public BookingController(BookingService bookingService,BookingRepository bookingRepo) {
        this.bookingService = bookingService;
        this.bookingRepo = bookingRepo;
    }

    @PostMapping
    public ResponseEntity<?> book(@RequestBody BookingRequest req,
                                  @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        try {
            BookingResponse response = bookingService.createBooking(req, idempotencyKey);
            return ResponseEntity.ok(response);
        } catch (FlightNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SoldOutException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (RateLimitExceededException e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(e.getMessage());
        }
    }
    // ✅ Real endpoint the Chatbot Service actually calls.
    // Accepts the common-dto shape (supplier/offer + passengers, no seat map)
    // and adapts it internally instead of requiring the chatbot to know
    // about seat numbers / fee breakdowns it doesn't have.
    @PostMapping("/flights")
    public ResponseEntity<?> bookFlightFromChat(
            @RequestBody CreateFlightBookingRequest req,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        try {
            BookingResponse response = bookingService.createBooking(req, idempotencyKey);
            return ResponseEntity.ok(response);
        } catch (FlightNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SoldOutException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (RateLimitExceededException e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/by-flight/{flightId}")
    public List<BookingInfo> getBookingsByFlight(@PathVariable String flightId) {

        return bookingRepo.findByFlightId(flightId)
                .stream()
                .map(b -> new BookingInfo(
                        b.getBookingUid(),
                        b.getUserId(),
                        b.getUserEmail(),
                        b.getFlightId(),
                        b.getPnr()
                ))
                .toList();
    }
    @PostMapping("/price-confirm")
    public ResponseEntity<?> confirmPrice(@RequestBody BookingRequest req) {

        PriceSnapshot breakup = bookingService.calculatePrice(req);

        return ResponseEntity.ok(breakup);
    }
    // ✅ AI uses this: "show my bookings"
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getBookingsByUser(@PathVariable String userId) {
        List<Booking> bookings = bookingRepo.findByUserId(userId);
        if (bookings.isEmpty()) {
            return ResponseEntity.ok(
                    Map.of("message", "No bookings found for user: " + userId));
        }
        return ResponseEntity.ok(bookings.stream()
                .map(b -> new BookingInfo(
                        b.getBookingUid(),
                        b.getUserId(),
                        b.getUserEmail(),
                        b.getFlightId(),
                        b.getPnr()
                )).toList());
    }

    // ✅ AI uses this: "show booking details BK-001"
    @GetMapping("/{bookingUid}")
    public ResponseEntity<?> getBookingByUid(@PathVariable String bookingUid) {
        return bookingRepo.findByBookingUid(bookingUid)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ AI uses this: "what's the status of flight-id"
    @GetMapping("/flight-status/{flightId}")
    public ResponseEntity<?> getFlightStatus(@PathVariable String flightId) {
        List<BookingInfo> bookings = bookingRepo.findByFlightId(flightId)
                .stream()
                .map(b -> new BookingInfo(
                        b.getBookingUid(),
                        b.getUserId(),
                        b.getUserEmail(),
                        b.getFlightId(),
                        b.getPnr()
                )).toList();
        return ResponseEntity.ok(bookings);
    }


}
