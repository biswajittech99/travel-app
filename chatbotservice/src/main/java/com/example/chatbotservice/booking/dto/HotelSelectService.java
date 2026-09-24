package com.example.chatbotservice.booking.dto;

import com.example.chatbotservice.booking.context.BookingSelectionContextService;
import com.example.chatbotservice.booking.context.SelectedHotelContext;
import com.example.chatbotservice.rag.dto.HotelOption;
import com.example.chatbotservice.rag.planner.service.TravelPlan;
import org.springframework.stereotype.Service;

@Service
public class HotelSelectService {

    private final BookingSelectionContextService contextService;

    public HotelSelectService(
            BookingSelectionContextService contextService
    ) {
        this.contextService = contextService;
    }

    public void selectHotel(
            String userId,
            String conversationId,
            HotelOption hotel,
            TravelPlan plan
    ) {

        SelectedHotelContext context =
                new SelectedHotelContext(

                        hotel.hotelId(),
                        hotel.hotelName(),

                        plan.destination(),

                        plan.travelDate(),
                        plan.returnDate(),

                        plan.adults(),
                        plan.rooms(),

                        hotel.originalPrice(),

                        hotel.currency()
                );

        contextService.saveHotelSelection(
                userId,
                conversationId,
                context
        );
    }
}
