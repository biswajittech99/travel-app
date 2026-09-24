//package com.example.chatbotservice.service;
//
//import org.springframework.ai.tool.annotation.Tool;
//import org.springframework.ai.tool.annotation.ToolParam;
//import org.springframework.stereotype.Component;
//
//import java.util.Map;
//
//@Component
//public class TravelTools {
//
//    private final ToolExecutorService toolExecutor;
//    private String currentUserId;
//
//    public TravelTools(ToolExecutorService toolExecutor) {
//        this.toolExecutor = toolExecutor;
//    }
//
//    public void setCurrentUserId(String userId) {
//        this.currentUserId = userId;
//    }
//
//    @Tool(description = """
//            Get all flight bookings for the current user.
//            Use when user asks 'my bookings', 'my trips',
//            'show my flights' or booking history.
//            """)
//    public String getUserBookings() {
//        // ✅ Uses currentUserId — no need to ask user for ID
//        return toolExecutor.execute("get_user_bookings",
//                Map.of("user_id", currentUserId),
//                currentUserId);
//    }
//
//    @Tool(description = """
//            Get detailed information about a specific booking.
//            Use when user asks about a specific booking ID or PNR.
//            """)
//    public String getBookingDetails(
//            @ToolParam(description = "The booking UID") String bookingId) {
//        return toolExecutor.execute("get_booking_details",
//                Map.of("booking_id", bookingId),
//                currentUserId);
//    }
//
//    @Tool(description = """
//            Search for available flights by origin,
//            destination and date.
//            Use when user wants to find or search flights.
//            Origin and destination must be IATA airport codes.
//            """)
//    public String searchFlights(
//            @ToolParam(description = "Origin airport IATA code e.g DEL, BOM, BLR")
//            String origin,
//            @ToolParam(description = "Destination airport IATA code e.g BOM, DEL, JAI")
//            String destination,
//            @ToolParam(description = "Travel date in YYYY-MM-DD format")
//            String date) {
//        return toolExecutor.execute("search_flights",
//                Map.of("origin", origin,
//                        "destination", destination,
//                        "date", date),
//                currentUserId);
//    }
//
//    @Tool(description = """
//            Get profile information for the current user.
//            Use when user asks about their account or profile.
//            """)
//    public String getUserProfile() {
//        return toolExecutor.execute("get_user_profile",
//                Map.of("user_id", currentUserId),
//                currentUserId);
//    }
//
//    @Tool(description = """
//            Get current flight status including delays.
//            Use when user asks about flight status or delays.
//            """)
//    public String getFlightStatus(
//            @ToolParam(description = "Flight number e.g AI302, 6E201")
//            String flightNumber) {
//        return toolExecutor.execute("get_flight_status",
//                Map.of("flight_number", flightNumber),
//                currentUserId);
//    }
//}
