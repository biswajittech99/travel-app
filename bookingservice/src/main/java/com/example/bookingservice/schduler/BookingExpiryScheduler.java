package com.example.bookingservice.schduler;

import com.example.bookingservice.entity.Booking;
import com.example.bookingservice.entity.FlightInventory;
import com.example.bookingservice.repo.BookingRepository;
import com.example.bookingservice.repo.FlightInventoryRepository;
import com.example.bookingservice.service.BookingService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class BookingExpiryScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(
                    BookingExpiryScheduler.class
            );

    private final BookingRepository bookingRepo;
    private final BookingService bookingService;

    public BookingExpiryScheduler(
            BookingRepository bookingRepo,
            BookingService bookingService
    ) {
        this.bookingRepo = bookingRepo;
        this.bookingService = bookingService;
    }


    @Scheduled(fixedRate = 60_000)
    public void expireUnpaidBookings() {

        List<Booking> expiredBookings =
                bookingRepo.findByStatusAndExpiresAtBefore(
                        "PAYMENT_PENDING",
                        LocalDateTime.now()
                );

        if (expiredBookings.isEmpty()) {

            return;
        }

        log.info(
                "Found {} expired unpaid bookings",
                expiredBookings.size()
        );


        for (Booking booking : expiredBookings) {

            try {

                log.warn(
                        "⏰ Expiring booking {}",
                        booking.getBookingUid()
                );


                // Reuse centralized expiry logic
                bookingService.expireBooking(
                        booking.getBookingUid()
                );


                log.info(
                        "Booking {} successfully expired",
                        booking.getBookingUid()
                );

            } catch (Exception ex) {

                log.error(
                        "Failed to expire booking {}",
                        booking.getBookingUid(),
                        ex
                );
            }
        }
    }


}
