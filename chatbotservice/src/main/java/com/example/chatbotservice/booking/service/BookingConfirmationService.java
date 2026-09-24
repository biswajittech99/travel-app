package com.example.chatbotservice.booking.service;

import com.example.chatbotservice.booking.client.SearchServiceClient;
import com.example.chatbotservice.booking.context.BookingSelectionContextService;
import com.example.chatbotservice.booking.context.SelectedFlightContext;
import com.example.chatbotservice.booking.context.SelectedHotelContext;
import com.example.chatbotservice.booking.dto.ConfirmationResult;
import org.example.common.dto.FlightRevalidationRequest;
import org.example.common.dto.FlightRevalidationResponse;
import org.example.common.dto.HotelRevalidationRequest;
import org.example.common.dto.HotelRevalidationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BookingConfirmationService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    BookingConfirmationService.class
            );

    private final BookingSelectionContextService contextService;
    private final SearchServiceClient searchServiceClient;

    public BookingConfirmationService(

            BookingSelectionContextService contextService,

            SearchServiceClient searchServiceClient
    ) {

        this.contextService = contextService;
        this.searchServiceClient = searchServiceClient;
    }

    public ConfirmationResult confirm(
            String userId,
            String conversationId
    ) {

        // =====================================================
        // 1. CHECK SELECTED FLIGHT
        // =====================================================

        var flight =
                contextService.getFlightSelection(
                        userId,
                        conversationId
                );

        if (flight.isPresent()) {

            return revalidateFlight(
                    flight.get()
            );
        }

        // =====================================================
        // 2. CHECK SELECTED HOTEL
        // =====================================================

        var hotel =
                contextService.getHotelSelection(
                        userId,
                        conversationId
                );

        if (hotel.isPresent()) {

            return revalidateHotel(
                    hotel.get()
            );
        }

        return ConfirmationResult.failure(
                "No flight or hotel has been selected for booking."
        );
    }

    // =====================================================
    // FLIGHT REVALIDATION
    // =====================================================

    private ConfirmationResult revalidateFlight(
            SelectedFlightContext context
    ) {

        FlightRevalidationRequest request =
                new FlightRevalidationRequest(

                        null,

                        context.supplier(),

                        context.flightId(),

                        context.supplierOfferId(),

                        context.flightNumber(),

                        context.origin(),

                        context.destination(),

                        context.departure(),

                        context.displayedPrice(),

                        context.currency(),

                        context.passengers()
                );

        FlightRevalidationResponse response =
                searchServiceClient
                        .revalidateFlight(request);

        if (!response.available()) {

            return ConfirmationResult.failure(
                    response.message()
            );
        }

        if (response.priceChanged()) {

            return ConfirmationResult.priceChanged(
                    context.displayedPrice(),
                    response.currentPrice(),
                    response.currency(),
                    response.message()
            );
        }

        return ConfirmationResult.readyForBooking(
                "Flight is available. Latest price is "
                        + response.currentPrice()
                        + " "
                        + response.currency()
        );
    }

    // =====================================================
    // HOTEL REVALIDATION
    // =====================================================

    private ConfirmationResult revalidateHotel(
            SelectedHotelContext context
    ) {

        HotelRevalidationRequest request =
                new HotelRevalidationRequest(

                        context.hotelId(),

                        context.hotelName(),

                        context.city(),

                        context.checkIn(),

                        context.checkOut(),

                        context.adults(),

                        context.rooms(),

                        context.originalPrice(),

                        context.currency()
                );

        HotelRevalidationResponse response =
                searchServiceClient
                        .revalidateHotel(request);

        if (!response.available()) {

            return ConfirmationResult.failure(
                    response.message()
            );
        }

        boolean priceChanged =
                context.originalPrice() != null
                        && response.currentPrice() != null
                        && context.originalPrice()
                        .compareTo(
                                response.currentPrice()
                        ) != 0;

        if (priceChanged) {

            return ConfirmationResult.priceChanged(
                    context.originalPrice(),
                    response.currentPrice(),
                    response.currency(),
                    "Hotel price has changed. Please accept the new price."
            );
        }

        return ConfirmationResult.readyForBooking(
                "Hotel is available at "
                        + response.currentPrice()
                        + " "
                        + response.currency()
        );
    }
}
