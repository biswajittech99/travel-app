package com.example.bookingservice.service;

import com.example.bookingservice.entity.Booking;
import com.example.bookingservice.entity.HotelBooking;
import org.example.common.dto.BookingConfirmedEvent;
import org.example.common.dto.BookingFailedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class BookingEventProducer {
    private static final String CONFIRMED_TOPIC = "booking-confirmed-topic";
    private static final String FAILED_TOPIC = "booking-failed-topic";

    private final KafkaTemplate<String, BookingConfirmedEvent> kafkaTemplate;
    private final KafkaTemplate<String, BookingFailedEvent> failedKafkaTemplate;

    public BookingEventProducer(
            KafkaTemplate<String, BookingConfirmedEvent> kafkaTemplate,
            KafkaTemplate<String, BookingFailedEvent> failedKafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.failedKafkaTemplate = failedKafkaTemplate;
    }

    public void publishBookingConfirmed(Booking booking) {

        BookingConfirmedEvent event = new BookingConfirmedEvent(
                booking.getBookingUid(),
                booking.getUserId(),
                booking.getUserEmail(),
                booking.getFlightId(),
                booking.getSeats(),
                booking.getBaseFare(),
                booking.getBaggageFee(),
                booking.getSeatFee(),
                booking.getConvenienceFee(),
                booking.getGst(),
                booking.getPrice(),
                booking.getPnr());

        kafkaTemplate.send(CONFIRMED_TOPIC, booking.getBookingUid(), event);
        System.out.println("✅ BookingConfirmedEvent sent: " + event);
    }

    public void publishBookingFailed(Booking booking, String reason) {

        BookingFailedEvent event = new BookingFailedEvent(
                booking.getBookingUid(),
                booking.getUserId(),
                booking.getUserEmail(),
                booking.getFlightId(),
                reason);

        failedKafkaTemplate.send(FAILED_TOPIC, booking.getBookingUid(), event);
        System.out.println("❌ BookingFailedEvent sent: " + event);
    }

    /**
     * Hotel bookings reuse the SAME BookingConfirmedEvent/BookingFailedEvent
     * DTOs and the SAME topics as flights, since chatbot's
     * BookingNotificationListener only ever reads bookingUid/pnr/totalAmount/
     * reason from these — it doesn't care about flight-specific fields.
     * hotelId goes into the "flightId" slot, rooms into "seats". This is a
     * deliberate simplification for now; if/when this needs to be a real
     * separate stream, rename BookingConfirmedEvent's fields to something
     * generic (e.g. referenceId) and update both producers/consumers.
     */
    public void publishHotelBookingConfirmed(HotelBooking booking) {

        BookingConfirmedEvent event = new BookingConfirmedEvent(
                booking.getBookingUid(),
                booking.getUserId(),
                booking.getUserEmail(),
                booking.getHotelId(),
                booking.getRooms(),
                booking.getPricePerNight(),
                0,
                0,
                booking.getConvenienceFee(),
                booking.getGst(),
                booking.getTotalAmount(),
                booking.getPnr());

        kafkaTemplate.send(CONFIRMED_TOPIC, booking.getBookingUid(), event);
        System.out.println("✅ Hotel BookingConfirmedEvent sent: " + event);
    }

    public void publishHotelBookingFailed(HotelBooking booking, String reason) {

        BookingFailedEvent event = new BookingFailedEvent(
                booking.getBookingUid(),
                booking.getUserId(),
                booking.getUserEmail(),
                booking.getHotelId(),
                reason);

        failedKafkaTemplate.send(FAILED_TOPIC, booking.getBookingUid(), event);
        System.out.println("❌ Hotel BookingFailedEvent sent: " + event);
    }
}
