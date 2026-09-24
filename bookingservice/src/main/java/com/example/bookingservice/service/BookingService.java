package com.example.bookingservice.service;


import com.example.bookingservice.dto.BookingRequest;
import com.example.bookingservice.dto.BookingResponse;
import com.example.bookingservice.dto.PriceSnapshot;
import com.example.bookingservice.entity.Booking;
import com.example.bookingservice.entity.BookingAudit;
import com.example.bookingservice.entity.FlightInventory;
import com.example.bookingservice.repo.BookingAuditRepository;
import com.example.bookingservice.repo.BookingRepository;
import com.example.bookingservice.repo.FlightInventoryRepository;
import org.example.common.dto.CreateFlightBookingRequest;
import org.example.common.dto.PaymentRequest;
import org.example.common.dto.PaymentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BookingService {

    private final FlightInventoryRepository inventoryRepo;
    private final BookingRepository bookingRepo;
    private final BookingAuditRepository auditRepo;
    private final RedisRateLimiter rateLimiter;
    private final BookingEventProducer bookingEventProducer;
    private final KafkaTemplate<String, PaymentRequest> paymentKafkaTemplate;

    private final Logger log = LoggerFactory.getLogger(getClass());

    public BookingService(
            FlightInventoryRepository inventoryRepo,
            BookingRepository bookingRepo,
            BookingAuditRepository auditRepo,
            RedisRateLimiter rateLimiter,
            BookingEventProducer bookingEventProducer,
            KafkaTemplate<String, PaymentRequest> paymentKafkaTemplate
    ) {
        this.inventoryRepo = inventoryRepo;
        this.bookingRepo = bookingRepo;
        this.auditRepo = auditRepo;
        this.rateLimiter = rateLimiter;
        this.bookingEventProducer = bookingEventProducer;
        this.paymentKafkaTemplate = paymentKafkaTemplate;
    }

    /**
     * STEP 1:
     *
     * Create booking
     * ↓
     * Reserve seats
     * ↓
     * PAYMENT_PENDING
     * ↓
     * Send payment request
     */
    @Transactional
    public BookingResponse createBooking(
            BookingRequest req,
            String idempotencyKey
    ) {

        // =====================================================
        // 1. IDEMPOTENCY CHECK
        // =====================================================

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {

            Optional<Booking> existing =
                    bookingRepo.findByIdempotencyKey(idempotencyKey);

            if (existing.isPresent()) {

                log.info(
                        "Returning existing booking for idempotency key: {}",
                        idempotencyKey
                );

                return toResponse(existing.get());
            }
        }


        // =====================================================
        // 2. RATE LIMIT
        // =====================================================

        if (!rateLimiter.tryAcquire(req.userId())) {

            throw new RateLimitExceededException(
                    "Too many booking attempts"
            );
        }


        // =====================================================
        // 3. VALIDATE REQUEST
        // =====================================================

        if (req.seatNumbers() == null ||
                req.seatNumbers().isEmpty()) {

            throw new IllegalArgumentException(
                    "At least one seat must be selected"
            );
        }

        int seatsRequested =
                req.seatNumbers().size();


        // =====================================================
        // 4. RESERVE INVENTORY
        // IMPORTANT:
        // This deducts from seatsAvailable and adds to
        // seatsReserved using pessimistic locking.
        // =====================================================

        reserveFlightSeats(
                req.flightId(),
                seatsRequested
        );


        // =====================================================
        // 5. CALCULATE PRICE
        // =====================================================

        PriceSnapshot breakup =
                calculatePrice(req);


        // =====================================================
        // 6. CREATE BOOKING
        // =====================================================

        Booking booking = new Booking();

        booking.setBookingUid(
                UUID.randomUUID().toString()
        );

        booking.setUserId(
                req.userId()
        );

        booking.setFlightId(
                req.flightId()
        );

        booking.setSeats(
                seatsRequested
        );

        booking.setSeatNumbers(
                req.seatNumbers()
        );

        booking.setStatus(
                "PAYMENT_PENDING"
        );

        booking.setIdempotencyKey(
                idempotencyKey
        );


        // =====================================================
        // 7. SAVE FULL PRICE SNAPSHOT
        // =====================================================

        booking.setBaseFare(
                breakup.baseFare()
        );

        booking.setSeatFee(
                breakup.seatFee()
        );

        booking.setBaggageFee(
                breakup.baggageFee()
        );

        booking.setMealFee(
                breakup.mealFee()
        );

        booking.setInsuranceFee(
                breakup.insuranceFee()
        );

        booking.setConvenienceFee(
                breakup.convenienceFee()
        );

        booking.setGst(
                breakup.gst()
        );

        booking.setPrice(
                breakup.totalAmount()
        );


        // =====================================================
        // 8. USER + EXPIRY
        // =====================================================

        booking.setUserEmail(
                req.userEmail()
        );

        booking.setExpiresAt(
                LocalDateTime.now()
                        .plusMinutes(5)
        );


        // =====================================================
        // 9. SAVE BOOKING
        // =====================================================

        bookingRepo.save(booking);


        // =====================================================
        // 10. AUDIT
        // =====================================================

        auditRepo.save(
                new BookingAudit(
                        booking.getBookingUid(),
                        booking.getUserId(),
                        booking.getFlightId(),
                        booking.getSeats(),
                        true,
                        "Booking created and seats reserved. Awaiting payment."
                )
        );


        // =====================================================
        // 11. CREATE PAYMENT REQUEST
        // =====================================================

        PaymentRequest payReq =
                new PaymentRequest(
                        booking.getBookingUid(),
                        booking.getUserId(),
                        breakup.totalAmount(),
                        "INR",
                        req.paymentMethod(),
                        req.userEmail()
                );


        // =====================================================
        // 12. SEND PAYMENT EVENT
        // =====================================================

        paymentKafkaTemplate
                .send(
                        "payment.request",
                        booking.getBookingUid(),
                        payReq
                )
                .whenComplete((result, exception) -> {

                    if (exception == null) {

                        log.info(
                                "Payment request sent for booking: {}",
                                booking.getBookingUid()
                        );

                    } else {

                        log.error(
                                "Failed to send payment request for booking: {}",
                                booking.getBookingUid(),
                                exception
                        );
                    }
                });


        return toResponse(booking);
    }


    /**
     * Chatbot-facing entry point.
     *
     * Accepts the common-dto CreateFlightBookingRequest (supplier/offer
     * shape, no explicit seat numbers, no fee breakdown) and adapts it
     * onto the exact same reservation + pricing + payment-request pipeline
     * as createBooking(BookingRequest, ...) above, so there is only ONE
     * place that owns inventory locking and pricing.
     */
    @Transactional
    public BookingResponse createBooking(
            CreateFlightBookingRequest req,
            String idempotencyKey
    ) {

        // 1. IDEMPOTENCY
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<Booking> existing =
                    bookingRepo.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                log.info("Returning existing booking for idempotency key: {}", idempotencyKey);
                return toResponse(existing.get());
            }
        }

        // 2. RATE LIMIT
        if (!rateLimiter.tryAcquire(req.userId())) {
            throw new RateLimitExceededException("Too many booking attempts");
        }

        // 3. VALIDATE
        int seatsRequested = req.passengers() == null ? 0 : req.passengers();
        if (seatsRequested < 1) {
            throw new IllegalArgumentException("At least one passenger is required");
        }
        if (req.paymentMethod() == null || req.paymentMethod().isBlank()) {
            throw new IllegalArgumentException("paymentMethod is required");
        }
        if (req.userEmail() == null || req.userEmail().isBlank()) {
            throw new IllegalArgumentException("userEmail is required");
        }

        // 4. RESERVE INVENTORY (same locked path as the other flow)
        reserveFlightSeats(req.flightId(), seatsRequested);

        // 5. PRICE — authoritative, calculated server-side from FlightInventory,
        // never trusted from req.price() (that's only the price the user saw
        // at revalidation time; the charge amount must come from us).
        // No named seats / meals / baggage / insurance in this flow yet,
        // so we generate placeholder seat labels and zero the optional extras.
        List<String> seatNumbers = new java.util.ArrayList<>();
        for (int i = 1; i <= seatsRequested; i++) {
            seatNumbers.add("PAX-" + i);
        }

        BookingRequest priceReq = new BookingRequest(
                req.userId(),
                req.flightId(),
                seatNumbers,
                0,      // mealSelected
                0,      // extraBaggageKg
                false,  // insuranceSelected
                null,
                req.paymentMethod(),
                req.userEmail()
        );

        PriceSnapshot breakup = calculatePrice(priceReq);

        // 6. CREATE BOOKING
        Booking booking = new Booking();
        booking.setBookingUid(UUID.randomUUID().toString());
        booking.setUserId(req.userId());
        booking.setFlightId(req.flightId());
        booking.setSeats(seatsRequested);
        booking.setSeatNumbers(seatNumbers);
        booking.setStatus("PAYMENT_PENDING");
        booking.setIdempotencyKey(idempotencyKey);

        booking.setBaseFare(breakup.baseFare());
        booking.setSeatFee(breakup.seatFee());
        booking.setBaggageFee(breakup.baggageFee());
        booking.setMealFee(breakup.mealFee());
        booking.setInsuranceFee(breakup.insuranceFee());
        booking.setConvenienceFee(breakup.convenienceFee());
        booking.setGst(breakup.gst());
        booking.setPrice(breakup.totalAmount());

        booking.setUserEmail(req.userEmail());
        booking.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        bookingRepo.save(booking);

        auditRepo.save(new BookingAudit(
                booking.getBookingUid(),
                booking.getUserId(),
                booking.getFlightId(),
                booking.getSeats(),
                true,
                "Booking created via chatbot and seats reserved. Awaiting payment."
        ));

        // 7. PAYMENT REQUEST — same topic, same downstream saga
        PaymentRequest payReq = new PaymentRequest(
                booking.getBookingUid(),
                booking.getUserId(),
                breakup.totalAmount(),
                "INR",
                req.paymentMethod(),
                req.userEmail()
        );

        paymentKafkaTemplate.send("payment.request", booking.getBookingUid(), payReq)
                .whenComplete((result, exception) -> {
                    if (exception == null) {
                        log.info("Payment request sent for booking: {}", booking.getBookingUid());
                    } else {
                        log.error("Failed to send payment request for booking: {}",
                                booking.getBookingUid(), exception);
                    }
                });

        return toResponse(booking);
    }


    /**
     * STEP 2:
     *
     * Reserve seats before payment.
     *
     * Before:
     * available = 10
     * reserved  = 0
     *
     * User reserves 2:
     *
     * available = 8
     * reserved  = 2
     */
    @Transactional
    public void reserveFlightSeats(
            String flightId,
            int requestedSeats
    ) {

        FlightInventory inventory =
                inventoryRepo.findByFlightIdForUpdate(flightId)
                        .orElseThrow(() ->
                                new FlightNotFoundException(
                                        "Flight not found"
                                )
                        );


        if (inventory.getSeatsAvailable() < requestedSeats) {

            throw new InsufficientSeatsException(
                    "Not enough seats available"
            );
        }


        inventory.setSeatsAvailable(
                inventory.getSeatsAvailable()
                        - requestedSeats
        );

        inventory.setSeatsReserved(
                inventory.getSeatsReserved()
                        + requestedSeats
        );


        inventoryRepo.save(inventory);

        log.info(
                "Reserved {} seats for flight {}. Available={}, Reserved={}",
                requestedSeats,
                flightId,
                inventory.getSeatsAvailable(),
                inventory.getSeatsReserved()
        );
    }


    /**
     * STEP 3:
     *
     * Called when payment succeeds.
     *
     * IMPORTANT:
     * Do NOT deduct seatsAvailable again.
     *
     * The seats were already reserved.
     *
     * Before confirmation:
     *
     * available = 8
     * reserved = 2
     *
     * After confirmation:
     *
     * available = 8
     * reserved = 0
     */

    @Transactional
    public void confirmBookingAfterPayment(
            PaymentResult result
    ) {

        Booking booking =
                bookingRepo.findByBookingUid(
                                result.bookingUid()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Booking not found: "
                                                + result.bookingUid()
                                )
                        );


        // Idempotency protection
        if ("CONFIRMED".equals(booking.getStatus())) {

            log.info(
                    "Booking {} already confirmed",
                    booking.getBookingUid()
            );

            return;
        }


        // Do not confirm cancelled/expired bookings
        if (!"PAYMENT_PENDING".equals(
                booking.getStatus()
        )) {

            log.warn(
                    "Booking {} cannot be confirmed. Current status: {}",
                    booking.getBookingUid(),
                    booking.getStatus()
            );

            return;
        }


        // Check expiry before confirmation
        if (booking.getExpiresAt() != null &&
                booking.getExpiresAt()
                        .isBefore(LocalDateTime.now())) {

            log.warn(
                    "Booking {} expired before payment confirmation",
                    booking.getBookingUid()
            );

            releaseReservedSeats(booking);

            booking.setStatus("EXPIRED");

            bookingRepo.save(booking);

            return;
        }


        FlightInventory inventory =
                inventoryRepo.findByFlightIdForUpdate(
                                booking.getFlightId()
                        )
                        .orElseThrow(() ->
                                new FlightNotFoundException(
                                        "Flight inventory not found"
                                )
                        );


        // Only remove from reserved inventory
        int updatedReserved =
                Math.max(
                        0,
                        inventory.getSeatsReserved()
                                - booking.getSeats()
                );

        inventory.setSeatsReserved(
                updatedReserved
        );
        // Confirm booking
        booking.setStatus("CONFIRMED");

        booking.setPnr(
                generatePNR()
        );


        inventoryRepo.save(inventory);

        bookingRepo.save(booking);


        // =====================================================
        // AUDIT
        // =====================================================

        auditRepo.save(
                new BookingAudit(
                        booking.getBookingUid(),
                        booking.getUserId(),
                        booking.getFlightId(),
                        booking.getSeats(),
                        true,
                        "Payment successful and booking confirmed"
                )
        );


        // =====================================================
        // PUBLISH BOOKING CONFIRMED EVENT
        // Notification Service should consume this event
        // =====================================================

        bookingEventProducer
                .publishBookingConfirmed(
                        booking
                );


        log.info(
                "Booking confirmed: {}, PNR: {}",
                booking.getBookingUid(),
                booking.getPnr()
        );
    }


    /**
     * STEP 4:
     *
     * Payment failed.
     *
     * Release reserved seats.
     */
    @Transactional
    public void handlePaymentFailure(
            PaymentResult result
    ) {

        Booking booking =
                bookingRepo.findByBookingUid(
                                result.bookingUid()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Booking not found: "
                                                + result.bookingUid()
                                )
                        );


        // Idempotency protection
        if ("PAYMENT_FAILED".equals(
                booking.getStatus()
        )) {

            log.info(
                    "Payment failure already processed for booking {}",
                    booking.getBookingUid()
            );

            return;
        }


        // Do not release seats for already confirmed booking
        if ("CONFIRMED".equals(
                booking.getStatus()
        )) {

            log.warn(
                    "Ignoring payment failure because booking {} is already confirmed",
                    booking.getBookingUid()
            );

            return;
        }


        releaseReservedSeats(booking);


        booking.setStatus(
                "PAYMENT_FAILED"
        );


        bookingRepo.save(booking);


        auditRepo.save(
                new BookingAudit(
                        booking.getBookingUid(),
                        booking.getUserId(),
                        booking.getFlightId(),
                        booking.getSeats(),
                        false,
                        "Payment failed. Reserved seats released."
                )
        );


        // =====================================================
        // PUBLISH BOOKING FAILED EVENT
        // Chatbot Service should consume this to notify the user
        // =====================================================

        bookingEventProducer.publishBookingFailed(
                booking,
                result.errorMessage()
        );


        log.info(
                "Payment failed for booking {}. Seats released.",
                booking.getBookingUid()
        );
    }


    /**
     * STEP 5:
     *
     * Release reserved seats.
     *
     * Before:
     * available = 8
     * reserved = 2
     *
     * After:
     * available = 10
     * reserved = 0
     */
    @Transactional
    public void releaseReservedSeats(
            Booking booking
    ) {

        FlightInventory inventory =
                inventoryRepo.findByFlightIdForUpdate(
                                booking.getFlightId()
                        )
                        .orElseThrow(() ->
                                new FlightNotFoundException(
                                        "Flight inventory not found"
                                )
                        );


        inventory.setSeatsAvailable(
                inventory.getSeatsAvailable()
                        + booking.getSeats()
        );


        inventory.setSeatsReserved(
                Math.max(
                        0,
                        inventory.getSeatsReserved()
                                - booking.getSeats()
                )
        );


        inventoryRepo.save(inventory);


        log.info(
                "Released {} seats for flight {}. Available={}, Reserved={}",
                booking.getSeats(),
                booking.getFlightId(),
                inventory.getSeatsAvailable(),
                inventory.getSeatsReserved()
        );
    }


    /**
     * STEP 6:
     *
     * Called by scheduler when payment timeout expires.
     */
    @Transactional
    public void expireBooking(
            String bookingUid
    ) {

        Booking booking =
                bookingRepo.findByBookingUid(bookingUid)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Booking not found"
                                )
                        );


        if (!"PAYMENT_PENDING".equals(
                booking.getStatus()
        )) {

            return;
        }


        releaseReservedSeats(booking);


        booking.setStatus(
                "EXPIRED"
        );


        bookingRepo.save(booking);


        auditRepo.save(
                new BookingAudit(
                        booking.getBookingUid(),
                        booking.getUserId(),
                        booking.getFlightId(),
                        booking.getSeats(),
                        false,
                        "Payment timeout. Booking expired and seats released."
                )
        );


        log.info(
                "Booking expired: {}",
                bookingUid
        );
    }


    /**
     * Calculate immutable booking price snapshot.
     */
    public PriceSnapshot calculatePrice(
            BookingRequest req
    ) {

        FlightInventory inv =
                inventoryRepo.findByFlightId(
                                req.flightId()
                        )
                        .orElseThrow(() ->
                                new FlightNotFoundException(
                                        "Flight not found"
                                )
                        );


        int seatCount =
                req.seatNumbers().size();


        double baseFare =
                inv.getPrice()
                        .doubleValue()
                        * seatCount;


        double seatFee =
                seatCount * 300;


        double mealFee =
                req.mealSelected() > 0
                        ? req.mealSelected()
                        : 0;


        double baggageFee =
                req.extraBaggageKg() * 100;


        double insuranceFee =
                req.insuranceSelected()
                        ? 199
                        : 0;


        double convenienceFee = 99;


        double taxes =
                baseFare * 0.18;


        double total =
                baseFare
                        + seatFee
                        + baggageFee
                        + mealFee
                        + insuranceFee
                        + convenienceFee
                        + taxes;


        return new PriceSnapshot(
                baseFare,
                seatFee,
                baggageFee,
                mealFee,
                insuranceFee,
                convenienceFee,
                taxes,
                total
        );
    }


    private BookingResponse toResponse(
            Booking booking
    ) {

        return new BookingResponse(
                booking.getBookingUid(),
                booking.getUserId(),
                booking.getFlightId(),
                booking.getSeats(),
                booking.getStatus()
        );
    }


    private String generatePNR() {

        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 6)
                .toUpperCase();
    }




}
