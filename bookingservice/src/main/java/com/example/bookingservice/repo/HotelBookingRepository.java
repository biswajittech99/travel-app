package com.example.bookingservice.repo;

import com.example.bookingservice.entity.HotelBooking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface HotelBookingRepository extends JpaRepository<HotelBooking,Long> {

    Optional<HotelBooking> findByIdempotencyKey(String idempotencyKey);
    Optional<HotelBooking> findByBookingUid(String bookingUid);
    List<HotelBooking> findByStatusAndExpiresAtBefore(String status, LocalDateTime time);
    List<HotelBooking> findByUserId(String userId);
}
