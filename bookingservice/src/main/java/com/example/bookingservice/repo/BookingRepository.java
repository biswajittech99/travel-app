package com.example.bookingservice.repo;

import com.example.bookingservice.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking,Long> {

    Optional<Booking> findByIdempotencyKey(String idempotencyKey);
    List<Booking> findByStatusAndExpiresAtBefore(
            String status,
            LocalDateTime time
    );
    Optional<Booking> findByBookingUid(String bookingUid);
    List<Booking> findByFlightId(String flightId);
    List<Booking> findByUserId(String userId);
    List<Booking> findByUserIdAndStatus(String userId, String status);


}
