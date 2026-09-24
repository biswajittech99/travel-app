package com.example.chatbotservice.rag.formatter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class LiveSearchFormatter {


    private final ObjectMapper mapper;

    public LiveSearchFormatter(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    // =====================================================
    // HOTEL SEARCH
    // =====================================================

    public String formatHotelSearch(String json) {

        try {

            JsonNode hotels = mapper.readTree(json);

            if (!hotels.isArray() || hotels.isEmpty()) {
                return "LIVE HOTEL SEARCH RESULTS\n\nNo hotels found.";
            }

            StringBuilder summary = new StringBuilder();

            summary.append("LIVE HOTEL SEARCH RESULTS\n\n");

            int limit = Math.min(10, hotels.size());

            for (int i = 0; i < limit; i++) {

                JsonNode hotel = hotels.get(i);

                summary.append(i + 1)
                        .append(". ")
                        .append(text(hotel, "hotelName", "Unknown hotel"))
                        .append("\n");

                String city =
                        text(hotel, "city", null);

                if (city != null && !city.isBlank()) {

                    summary.append("City: ")
                            .append(city)
                            .append("\n");
                }

                String rating =
                        formatRating(hotel);

                if (rating != null) {

                    summary.append("Rating: ")
                            .append(rating)
                            .append("\n");
                }

                String price =
                        formatPrice(hotel);

                summary.append("Price: ")
                        .append(price)
                        .append("\n");

                String currency =
                        text(hotel, "currency", null);

                if (currency != null
                        && !currency.isBlank()) {

                    summary.append("Currency: ")
                            .append(currency)
                            .append("\n");
                }

                String supplier =
                        text(hotel, "supplier", null);

                if (supplier != null
                        && !supplier.isBlank()) {

                    summary.append("Supplier: ")
                            .append(supplier)
                            .append("\n");
                }

                String availability =
                        formatAvailability(hotel);

                if (availability != null) {

                    summary.append("Availability: ")
                            .append(availability)
                            .append("\n");
                }

                summary.append("\n");
            }

            if (hotels.size() > limit) {

                summary.append(
                                "Only the top "
                        ).append(limit)
                        .append(" hotel results are included ")
                        .append("from ")
                        .append(hotels.size())
                        .append(" live results.\n");
            }

            return summary.toString();

        } catch (Exception e) {

            return """
                    LIVE HOTEL SEARCH RESULTS

                    Unable to format the live hotel response.
                    """;
        }
    }


    // =====================================================
    // FLIGHT SEARCH
    // =====================================================

    public String formatFlightSearch(String json) {

        try {

            JsonNode flights =
                    mapper.readTree(json);

            if (!flights.isArray()
                    || flights.isEmpty()) {

                return """
                        LIVE FLIGHT SEARCH RESULTS

                        No flights found.
                        """;
            }

            StringBuilder summary =
                    new StringBuilder();

            summary.append(
                    "LIVE FLIGHT SEARCH RESULTS\n\n"
            );

            int limit =
                    Math.min(10, flights.size());

            for (int i = 0;
                 i < limit;
                 i++) {

                JsonNode flight =
                        flights.get(i);

                summary.append(i + 1)
                        .append(". ");

                summary.append(
                        text(
                                flight,
                                "flightNumber",
                                "Unknown flight"
                        )
                );

                summary.append("\n");

                String from =
                        text(
                                flight,
                                "from",
                                "N/A"
                        );

                String to =
                        text(
                                flight,
                                "to",
                                "N/A"
                        );

                summary.append(
                                "Route: "
                        ).append(from)
                        .append(" → ")
                        .append(to)
                        .append("\n");

                String departure =
                        text(
                                flight,
                                "departure",
                                "N/A"
                        );

                summary.append(
                                "Departure: "
                        ).append(departure)
                        .append("\n");

                String arrival =
                        text(
                                flight,
                                "arrival",
                                "N/A"
                        );

                summary.append(
                                "Arrival: "
                        ).append(arrival)
                        .append("\n");

                summary.append(
                        "Price: "
                ).append(
                        formatPrice(flight)
                );

                String currency =
                        text(
                                flight,
                                "currency",
                                null
                        );

                if (currency != null
                        && !currency.isBlank()) {

                    summary.append(" ")
                            .append(currency);
                }

                summary.append("\n\n");
            }

            if (flights.size() > limit) {

                summary.append(
                                "Only the top "
                        ).append(limit)
                        .append(" flight results are included ")
                        .append("from ")
                        .append(flights.size())
                        .append(" live results.\n");
            }

            return summary.toString();

        } catch (Exception e) {

            return """
                    LIVE FLIGHT SEARCH RESULTS

                    Unable to format the live flight response.
                    """;
        }
    }


    // =====================================================
    // TEXT HELPER
    // =====================================================

    private String text(
            JsonNode node,
            String field,
            String defaultValue
    ) {

        JsonNode value =
                node.get(field);

        if (value == null
                || value.isNull()
                || value.isMissingNode()) {

            return defaultValue;
        }

        String text =
                value.asText();

        if (text == null
                || text.isBlank()) {

            return defaultValue;
        }

        return text;
    }


    // =====================================================
    // RATING
    // =====================================================

    private String formatRating(
            JsonNode hotel
    ) {

        JsonNode rating =
                hotel.get("rating");

        if (rating == null
                || rating.isNull()) {

            return null;
        }

        if (!rating.isNumber()) {

            String value =
                    rating.asText();

            return value.isBlank()
                    ? null
                    : value;
        }

        double value =
                rating.asDouble();

        if (value <= 0) {

            return null;
        }

        return String.format(
                "%.1f",
                value
        );
    }


    // =====================================================
    // PRICE
    // =====================================================

    private String formatPrice(
            JsonNode node
    ) {

        JsonNode price =
                node.get("price");

        if (price == null
                || price.isNull()) {

            return "Price not available";
        }

        if (price.isNumber()) {

            double value =
                    price.asDouble();

            if (value <= 0) {

                return "Price not available";
            }

            return String.format(
                    "₹%.2f",
                    value
            );
        }

        String value =
                price.asText();

        if (value == null
                || value.isBlank()
                || value.equals("0")
                || value.equals("0.0")) {

            return "Price not available";
        }

        return value;
    }


    // =====================================================
    // AVAILABILITY
    // =====================================================

    private String formatAvailability(
            JsonNode hotel
    ) {

        JsonNode rooms =
                hotel.get("availableRooms");

        if (rooms == null
                || rooms.isNull()) {

            return null;
        }

        if (!rooms.isNumber()) {

            String value =
                    rooms.asText();

            return value.isBlank()
                    ? null
                    : value;
        }

        int count =
                rooms.asInt();

        /*
         * Do not expose 0 as confirmed unavailability.
         * Zero may mean the supplier did not provide
         * availability information.
         */
        if (count <= 0) {

            return "Availability not provided";
        }

        return count + " rooms available";
    }

}

/*
2026-09-07T17:13:02.899+05:30  INFO 522 --- [CHATBOTSERVICE] [nio-8081-exec-6] c.e.c.rag.chat.TravelChatService         : Chat request received. conversationId=fe178f6b-be6b-4d2c-961c-5303a6fa8222
2026-09-07T17:13:04.803+05:30  INFO 522 --- [CHATBOTSERVICE] [nio-8081-exec-6] c.e.c.rag.chat.TravelChatService         : Extracted TravelPlan=TravelPlan[intent=HOTEL_SEARCH, origin=null, destination=null, travelDate=null, returnDate=null, adults=1, children=0, rooms=1, hotelName=null, bookingId=null, userQuestion=show which hote are avaiable room ]
========== TOOL ORCHESTRATOR ==========
Intent: HOTEL_SEARCH
Origin: null
Destination: null
Date: null
2026-09-07T17:13:07.316+05:30  INFO 522 --- [CHATBOTSERVICE] [nio-8081-exec-6] c.e.c.service.TravelToolOrchestrator     : Tool orchestrator received intent: HOTEL_SEARCH
2026-09-07T17:13:07.317+05:30  INFO 522 --- [CHATBOTSERVICE] [nio-8081-exec-6] c.e.c.service.TravelToolOrchestrator     : Executing HOTEL_SEARCH tool
2026-09-07T17:13:07.317+05:30  INFO 522 --- [CHATBOTSERVICE] [nio-8081-exec-6] c.e.c.service.TravelToolOrchestrator     : Executing HOTEL_SEARCH: null from null to null
2026-09-07T17:13:11.357+05:30 ERROR 522 --- [CHATBOTSERVICE] [nio-8081-exec-6] o.a.c.c.C.[.[.[/].[dispatcherServlet]    : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed: java.lang.NullPointerException: Cannot invoke "java.time.LocalDate.toString()" because the return value of "com.example.chatbotservice.rag.planner.service.TravelPlan.travelDate()" is null] with root cause

java.lang.NullPointerException: Cannot invoke "java.time.LocalDate.toString()" because the return value of "com.example.chatbotservice.rag.planner.service.TravelPlan.travelDate()" is null
	at com.example.chatbotservice.service.TravelToolOrchestrator.executeHotelSearch(TravelToolOrchestrator.java:230) ~[classes/:na]
	at com.example.chatbotservice.service.TravelToolOrchestrator.execute(TravelToolOrchestrator.java:78) ~[classes/:na]
	at com.example.chatbotservice.rag.chat.TravelChatService.chat(TravelChatService.java:241) ~[classes/:na]
	at com.example.chatbotservice.controller.ChatController.message(ChatController.java:77) ~[classes/:na]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[na:na]
	at org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:258) ~[spring-web-6.2.5.jar:6.2.5]
	at org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:191) ~[spring-web-6.2.5.jar:6.2.5]
	at org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:118) ~[spring-webmvc-6.2.5.jar:6.2.5]
	at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:986) ~[spring-webmvc-6.2.5.jar:6.2.5]
	at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:891) ~[spring-webmvc-6.2.5.jar:6.2.5]
	at org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:87) ~[spring-webmvc-6.2.5.jar:6.2.5]
	at org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:1089) ~[spring-webmvc-6.2.5.jar:6.2.5]
	at org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:979) ~[spring-webmvc-6.2.5.jar:6.2.5]
	at org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1014) ~[spring-webmvc-6.2.5.jar:6.2.5]
	at org.springframework.web.servlet.FrameworkServlet.doPost(FrameworkServlet.java:914) ~[spring-webmvc-6.2.5.jar:6.2.5]
	at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:590) ~[tomcat-embed-core-10.1.39.jar:6.0]
	at org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:885) ~[spring-webmvc-6.2.5.jar:6.2.5]
	at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:658) ~[tomcat-embed-core-10.1.39.jar:6.0]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:195) ~[tomcat-embed-core-10.1.39.jar:10.1.39]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140) ~[tomcat-embed-core-10.1.39.jar:10.1.39]
	at org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:51) ~[tomcat-embed-websocket-10.1.39.jar:10.1.39]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:164) ~[tomcat-embed-core-10.1.39.jar:10.1.39]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140) ~[tomcat-embed-core-10.1.39.jar:10.1.39]
	at org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:100) ~[spring-web-6.2.5.jar:6.2.5]
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-6.2.5.jar:6.2.5]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:164) ~[tomcat-embed-core-10.1.39.jar:10.1.39]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140) ~[tomcat-embed-core-10.1.39.jar:10.1.39]
	at org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:93) ~[spring-web-6.2.5.jar:6.2.5]
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-6.2.5.jar:6.2.5]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:164) ~[tomcat-embed-core-10.1.39.jar:10.1.39]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140) ~[tomcat-embed-core-10.1.39.jar:10.1.39]
	at org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:201) ~[spring-web-6.2.5.jar:6.2.5]
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-6.2.5.jar:6.2.5]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:164) ~[tomcat-embed-core-10.1.39.jar:10.1.39]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140) ~[tomcat-embed-core-10.1.39.jar:10.1.39]
	at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:167) ~[tomcat-embed-core-10.1.39.jar:10.1.39]
	at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:90) ~[tomcat-embed-core-10.1.39.jar:10.1.39]
	at org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:483) ~[tomcat-embed-core-10.1.39.jar:10.1.39]
	at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:115) ~[tomcat-embed-core-10.1.39.jar:10.1.39]
	at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:93) ~[tomcat-embed-core-10.1.39.jar:10.1.39]
	at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:74) ~[tomcat-embed-core-10.1.39.jar:10.1.39]
	at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:344) ~[tomcat-embed-core-10.1.39.jar:10.1.39]
	at org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:397) ~[tomcat-embed-core-10.1.39.jar:10.1.39]
	at org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:63) ~[tomcat-embed-core-10.1.39.jar:10.1.39]
	at org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:905) ~[tomcat-embed-core-10.1.39.jar:10.1.39]
	at org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1743) ~[tomcat-embed-core-10.1.39.jar:10.1.39]
	at org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:52) ~[tomcat-embed-core-10.1.39.jar:10.1.39]
	at org.apache.tomcat.util.threads.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1190) ~[tomcat-embed-core-10.1.39.jar:10.1.39]
	at org.apache.tomcat.util.threads.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:659) ~[tomcat-embed-core-10.1.39.jar:10.1.39]
	at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:63) ~[tomcat-embed-core-10.1.39.jar:10.1.39]
	at java.base/java.lang.Thread.run(Thread.java:1583) ~[na:na]


	search service-  "type" : "hotel",
    "name" : "DoubleTree by Hilton Hotel Goa - Arpora - Baga",
    "description" : "Airy rooms, some with balconies, in a casual hotel offering 2 restaurants, an outdoor pool & a spa.",
    "link" : "https://www.hilton.com/en/hotels/goiabdi-doubletree-goa-arpora-baga/?SEO_id=GMB-APAC-DI-GOIABDI",
    "property_token" : "ChcI9tCXv_3yu4hIGgsvZy8xdGR5emRiZxAB",
    "serpapi_property_details_link" : "https://serpapi.com/search.json?adults=2&check_in_date=2026-09-20&check_out_date=2026-09-22&children=0&currency=INR&engine=google_hotels&gl=us&hl=en&property_token=ChcI9tCXv_3yu4hIGgsvZy8xdGR5emRiZxAB&q=Goa",
    "gps_coordinates" : {
      "latitude" : 15.575674300000001,
      "longitude" : 73.7618006
    },
    "check_in_time" : "3:00 PM",
    "check_out_time" : "12:00 PM",
    "rate_per_night" : {
      "lowest" : "₹4,573",
      "extracted_lowest" : 4573
    },
    "total_rate" : {
      "lowest" : "₹9,147",
      "extracted_lowest" : 9147
    },
    "nearby_places" : [ {
      "name" : "Chauranganath Mandir",
      "transportations" : [ {
        "type" : "Walking",
        "duration" : "4 min"
      } ]
    }, {
      "name" : "Goa Dabolim International Airport",
      "transportations" : [ {
        "type" : "Taxi",
        "duration" : "1 hr 15 min"
      }, {
        "type" : "Public transport",
        "duration" : "2 hr 31 min"
      } ]
    }, {
      "name" : "Blue flame kitchen & Bar",
      "transportations" : [ {
        "type" : "Walking",
        "duration" : "2 min"
      } ]
    } ],
    "hotel_class" : "5-star hotel",
    "extracted_hotel_class" : 5,
    "images" : [ {
      "thumbnail" : "https://lh3.googleusercontent.com/gps-cs-s/AHRPTWkPlZZzcJ16WZdACMSkVZUzG7RD_iXAf2leYIYIj79ClmbmHHF4fH2hGPZobT9GumfE-9dfhCxLqsOdxYf3jgBYGlR1mhxaLNwwH0z2m1f39xfIN1g-0n1pfGzJEguotc_88HOG=s287-w287-h192-n-k-no-v1",
      "original_image" : "https://lh3.googleusercontent.com/gps-cs-s/AHRPTWkPlZZzcJ16WZdACMSkVZUzG7RD_iXAf2leYIYIj79ClmbmHHF4fH2hGPZobT9GumfE-9dfhCxLqsOdxYf3jgBYGlR1mhxaLNwwH0z2m1f39xfIN1g-0n1pfGzJEguotc_88HOG=s10000"
    }, {

 */



