package com.example.chatbotservice.booking;

import com.example.chatbotservice.rag.dto.HotelOption;
import com.example.chatbotservice.rag.dto.TravelOption;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BookingSelectionService {


    private final LiveSearchContextRedisService searchContextService;

    private final PendingBookingRedisService
            pendingBookingRedisService;

    private final FlightSelectionService
            flightSelectionService;

    private final ObjectMapper objectMapper;
    private final HotelSelectionService hotelSelectionService;

    public BookingSelectionService(
            LiveSearchContextRedisService searchContextService,
            PendingBookingRedisService pendingBookingRedisService,
            FlightSelectionService flightSelectionService,
            ObjectMapper objectMapper, HotelSelectionService hotelSelectionService
    ) {

        this.searchContextService =
                searchContextService;

        this.pendingBookingRedisService =
                pendingBookingRedisService;

        this.flightSelectionService =
                flightSelectionService;

        this.objectMapper =
                objectMapper;
        this.hotelSelectionService = hotelSelectionService;
    }

    public Optional<SelectedFlight> selectFlight(
            String userId,
            String conversationId,
            String question
    ) {

        Optional<LiveSearchContext> contextOptional =
                searchContextService.find(
                        conversationId
                );

        if (contextOptional.isEmpty()) {
            return Optional.empty();
        }

        LiveSearchContext context =
                contextOptional.get();

        if (context.type() !=
                PendingBookingType.FLIGHT) {

            return Optional.empty();
        }

        try {

            List<TravelOption> flights =
                    objectMapper.readValue(
                            context.searchData(),
                            new TypeReference<List<TravelOption>>() {}
                    );

            Optional<Integer> selection =
                    flightSelectionService.detectSelection(
                            question,
                            flights
                    );

            if (selection.isEmpty()) {
                return Optional.empty();
            }

            TravelOption flight =
                    flights.get(selection.get());

            SelectedFlight selectedFlight =
                    new SelectedFlight(
                            flight.supplier(),
                            flight.id(),
                            flight.id(),
                            flight.flightNumber(),
                            flight.from(),
                            flight.to(),
                            flight.departure(),
                            flight.arrival(),
                            flight.price(),
                            flight.currency(),
                            1
                    );

            PendingBookingContext pendingBooking =
                    new PendingBookingContext(


                            conversationId,

                            userId,

                            PendingBookingType.FLIGHT,

                            null,   // bookingData is not created yet

                            objectMapper.writeValueAsString(
                                    selectedFlight
                            ),

                            BookingWorkflowState.SELECTED,

                            null,   // paymentMethod not chosen yet

                            LocalDateTime.now()
                    );

            pendingBookingRedisService.save(
                    pendingBooking
            );

            return Optional.of(
                    selectedFlight
            );

        } catch (Exception ex) {

            throw new IllegalStateException(
                    "Failed to select flight",
                    ex
            );
        }
    }

    public Optional<SelectedHotel> selectHotel(
            String userId,
            String conversationId,
            String question
    ) {

        Optional<LiveSearchContext> contextOptional =
                searchContextService.find(conversationId);

        if (contextOptional.isEmpty()) {
            return Optional.empty();
        }

        LiveSearchContext context = contextOptional.get();

        // Only process hotel search context
        if (context.type() != PendingBookingType.HOTEL) {
            return Optional.empty();
        }

        try {

            // Read the hotel list saved after live search
            List<HotelOption> hotels =
                    objectMapper.readValue(
                            context.searchData(),
                            new TypeReference<List<HotelOption>>() {
                            }
                    );

            // Detect: first hotel / second hotel / option 2, etc.
            Optional<Integer> selection =
                    hotelSelectionService.detectSelection(
                            question,
                            hotels
                    );

            if (selection.isEmpty()) {
                return Optional.empty();
            }

            HotelOption hotel =
                    hotels.get(selection.get());

            SelectedHotel selectedHotel =
                    new SelectedHotel(
                            hotel.supplier(),
                            hotel.hotelId(),
                            hotel.hotelName(),
                            hotel.city(),
                            hotel.checkIn(),
                            hotel.checkOut(),
                            hotel.originalPrice(),
                            hotel.currency(),
                            hotel.adults(),
                            hotel.rooms()
                    );

            // Save selected hotel as pending booking
            PendingBookingContext pendingBooking =
                    new PendingBookingContext(

                            conversationId,

                            userId,

                            PendingBookingType.HOTEL,

                            null,   // bookingData not created yet

                            objectMapper.writeValueAsString(
                                    selectedHotel
                            ),

                            BookingWorkflowState.SELECTED,

                            null,   // paymentMethod not chosen yet

                            LocalDateTime.now()
                    );
            pendingBookingRedisService.save(
                    pendingBooking
            );
            return Optional.of(selectedHotel);

        } catch (Exception ex) {

            throw new IllegalStateException(
                    "Failed to select hotel",
                    ex
            );
        }
    }
}

