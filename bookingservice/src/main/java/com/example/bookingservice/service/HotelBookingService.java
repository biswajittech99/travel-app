package com.example.bookingservice.service;

import com.example.bookingservice.entity.HotelBooking;
import com.example.bookingservice.entity.HotelInventory;
import com.example.bookingservice.repo.HotelBookingRepository;
import com.example.bookingservice.repo.HotelInventoryRepository;
import org.example.common.dto.CreateHotelBookingRequest;
import org.example.common.dto.PaymentRequest;
import org.example.common.dto.PaymentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
public class HotelBookingService {

    private final HotelInventoryRepository inventoryRepo;
    private final HotelBookingRepository bookingRepo;
    private final RedisRateLimiter rateLimiter;
    private final BookingEventProducer bookingEventProducer;
    private final KafkaTemplate<String, PaymentRequest> paymentKafkaTemplate;

    private final Logger log = LoggerFactory.getLogger(getClass());

    public HotelBookingService(
            HotelInventoryRepository inventoryRepo,
            HotelBookingRepository bookingRepo,
            RedisRateLimiter rateLimiter,
            BookingEventProducer bookingEventProducer,
            KafkaTemplate<String, PaymentRequest> paymentKafkaTemplate) {
        this.inventoryRepo = inventoryRepo;
        this.bookingRepo = bookingRepo;
        this.rateLimiter = rateLimiter;
        this.bookingEventProducer = bookingEventProducer;
        this.paymentKafkaTemplate = paymentKafkaTemplate;
    }

    @Transactional
    public HotelBooking createBooking(CreateHotelBookingRequest req, String idempotencyKey) {

        // 1. IDEMPOTENCY
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<HotelBooking> existing = bookingRepo.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                log.info("Returning existing hotel booking for idempotency key: {}", idempotencyKey);
                return existing.get();
            }
        }

        // 2. RATE LIMIT
        if (!rateLimiter.tryAcquire(req.userId())) {
            throw new RateLimitExceededException("Too many booking attempts");
        }

        // 3. VALIDATE
        int rooms = req.rooms() == null ? 0 : req.rooms();
        int adults = req.adults() == null ? 0 : req.adults();
        if (rooms < 1) {
            throw new IllegalArgumentException("At least one room is required");
        }
        if (req.checkIn() == null || req.checkOut() == null || !req.checkOut().isAfter(req.checkIn())) {
            throw new IllegalArgumentException("checkOut must be after checkIn");
        }
        if (req.paymentMethod() == null || req.paymentMethod().isBlank()) {
            throw new IllegalArgumentException("paymentMethod is required");
        }
        if (req.userEmail() == null || req.userEmail().isBlank()) {
            throw new IllegalArgumentException("userEmail is required");
        }

        // 4. RESERVE ROOMS (pessimistic lock, same pattern as flight seats)
        HotelInventory inventory = inventoryRepo.findByHotelIdForUpdate(req.hotelId())
                .orElseThrow(() -> new HotelNotFoundException("Hotel not found: " + req.hotelId()));

        if (inventory.getRoomsAvailable() < rooms) {
            throw new InsufficientSeatsException("Not enough rooms available");
        }

        inventory.setRoomsAvailable(inventory.getRoomsAvailable() - rooms);
        inventory.setRoomsReserved(inventory.getRoomsReserved() + rooms);
        inventoryRepo.save(inventory);

        // 5. PRICE — authoritative, from HotelInventory, never from req.price()
        long nights = ChronoUnit.DAYS.between(req.checkIn(), req.checkOut());
        double pricePerNight = inventory.getPrice().doubleValue();
        double baseTotal = pricePerNight * nights * rooms;
        double convenienceFee = Math.round(baseTotal * 0.02 * 100.0) / 100.0; // 2%, same style as flight fees
        double gst = Math.round((baseTotal + convenienceFee) * 0.18 * 100.0) / 100.0; // 18% GST
        double totalAmount = baseTotal + convenienceFee + gst;

        // 6. CREATE BOOKING
        HotelBooking booking = new HotelBooking();
        booking.setBookingUid(UUID.randomUUID().toString());
        booking.setUserId(req.userId());
        booking.setHotelId(req.hotelId());
        booking.setHotelName(req.hotelName());
        booking.setCity(req.city());
        booking.setCheckIn(req.checkIn());
        booking.setCheckOut(req.checkOut());
        booking.setRooms(rooms);
        booking.setAdults(adults);
        booking.setStatus("PAYMENT_PENDING");
        booking.setIdempotencyKey(idempotencyKey);
        booking.setPricePerNight(pricePerNight);
        booking.setConvenienceFee(convenienceFee);
        booking.setGst(gst);
        booking.setTotalAmount(totalAmount);
        booking.setUserEmail(req.userEmail());
        booking.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        bookingRepo.save(booking);

        // 7. PAYMENT REQUEST — same topic, same downstream saga as flights
        PaymentRequest payReq = new PaymentRequest(
                booking.getBookingUid(),
                booking.getUserId(),
                totalAmount,
                "INR",
                req.paymentMethod(),
                req.userEmail());

        paymentKafkaTemplate.send("payment.request", booking.getBookingUid(), payReq)
                .whenComplete((result, exception) -> {
                    if (exception == null) {
                        log.info("Payment request sent for hotel booking: {}", booking.getBookingUid());
                    } else {
                        log.error("Failed to send payment request for hotel booking: {}",
                                booking.getBookingUid(), exception);
                    }
                });

        return booking;
    }

    @Transactional
    public void confirmBookingAfterPayment(PaymentResult result) {

        Optional<HotelBooking> optional = bookingRepo.findByBookingUid(result.bookingUid());
        if (optional.isEmpty()) {
            log.warn("No hotel booking found for bookingUid={}", result.bookingUid());
            return;
        }

        HotelBooking booking = optional.get();

        if ("CONFIRMED".equals(booking.getStatus())) {
            log.info("Hotel booking {} already CONFIRMED, ignoring duplicate event", booking.getBookingUid());
            return;
        }
        if ("EXPIRED".equals(booking.getStatus())) {
            log.warn("Payment succeeded for EXPIRED hotel booking {} — needs manual refund", booking.getBookingUid());
            return;
        }

        // release the room hold: rooms were already deducted from
        // roomsAvailable at reservation time, so on confirmation we just
        // clear them from roomsReserved (same release-not-double-deduct
        // pattern as reserveFlightSeats/confirmBookingAfterPayment for flights)
        inventoryRepo.findByHotelIdForUpdate(booking.getHotelId()).ifPresent(inv -> {
            inv.setRoomsReserved(Math.max(0, inv.getRoomsReserved() - booking.getRooms()));
            inventoryRepo.save(inv);
        });

        booking.setStatus("CONFIRMED");
        booking.setPaymentProviderId(result.providerPaymentId());
        booking.setPnr(generatePnr());
        bookingRepo.save(booking);

        bookingEventProducer.publishHotelBookingConfirmed(booking);

        log.info("Hotel booking {} confirmed.", booking.getBookingUid());
    }

    @Transactional
    public void handlePaymentFailure(PaymentResult result) {

        Optional<HotelBooking> optional = bookingRepo.findByBookingUid(result.bookingUid());
        if (optional.isEmpty()) {
            log.warn("No hotel booking found for bookingUid={}", result.bookingUid());
            return;
        }

        HotelBooking booking = optional.get();

        if ("PAYMENT_FAILED".equals(booking.getStatus()) || "CONFIRMED".equals(booking.getStatus())) {
            log.info("Hotel booking {} already resolved as {}, ignoring", booking.getBookingUid(), booking.getStatus());
            return;
        }

        // release the reserved rooms back to availability
        inventoryRepo.findByHotelIdForUpdate(booking.getHotelId()).ifPresent(inv -> {
            inv.setRoomsReserved(Math.max(0, inv.getRoomsReserved() - booking.getRooms()));
            inv.setRoomsAvailable(inv.getRoomsAvailable() + booking.getRooms());
            inventoryRepo.save(inv);
        });

        booking.setStatus("PAYMENT_FAILED");
        bookingRepo.save(booking);

        bookingEventProducer.publishHotelBookingFailed(booking, result.errorMessage());

        log.info("Payment failed for hotel booking {}. Rooms released.", booking.getBookingUid());
    }

    private String generatePnr() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }


}
