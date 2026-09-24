package com.example.chatbotservice.rag.chat;


import com.example.chatbotservice.booking.*;
import com.example.chatbotservice.booking.client.BookingServiceClient;
import com.example.chatbotservice.booking.client.SearchServiceClient;
import com.example.chatbotservice.booking.client.UserServiceClient;
import com.example.chatbotservice.entity.Conversation;
import com.example.chatbotservice.rag.advisor.TravelAdvisorChain;
import com.example.chatbotservice.rag.classifier.IntentClassifier;
import com.example.chatbotservice.rag.context.ConversationContextBuilder;
import com.example.chatbotservice.rag.context.PromptContext;
import com.example.chatbotservice.rag.formatter.LiveSearchFormatter;
import com.example.chatbotservice.rag.llm.LlmService;
import com.example.chatbotservice.rag.memory.ConversationMemoryService;
import com.example.chatbotservice.rag.memory.ConversationMessage;
import com.example.chatbotservice.rag.planner.IntentType;
import com.example.chatbotservice.rag.planner.TravelPlanExtractor;
import com.example.chatbotservice.rag.planner.TravelPlanner;
import com.example.chatbotservice.rag.planner.service.TravelPlan;
import com.example.chatbotservice.rag.prompt.PromptBuilder;
import com.example.chatbotservice.rag.tool.ToolResult;
import com.example.chatbotservice.rag.util.PromptTokenLimiter;
import com.example.chatbotservice.repo.ConversationRepository;
import com.example.chatbotservice.service.TravelToolOrchestrator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.common.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import static com.example.chatbotservice.booking.BookingWorkflowState.*;
import static com.example.chatbotservice.booking.service.PendingBookingStatus.READY_FOR_BOOKING;
import static com.example.chatbotservice.booking.service.PendingBookingStatus.SELECTED;

@Service
public class TravelChatService {

    private final ConversationRepository conversationRepository;

    private final ConversationMemoryService memoryService;

    private final TravelAdvisorChain advisorChain;

    private final PromptBuilder promptBuilder;

    private final LlmService llmService;

    private final LiveSearchFormatter formatter;

    private final ConversationContextBuilder contextBuilder;

    private final PromptTokenLimiter tokenLimiter;


    private final IntentClassifier intentClassifier;

    private final TravelPlanner travelPlanner;

    private final ObjectMapper objectMapper;

    private final LiveSearchContextRedisService
            liveSearchContextRedisService;
    private final BookingSelectionService
            bookingSelectionService;

    private final PendingBookingRedisService pendingBookingRedisService;

    private final TravelPlanExtractor planExtractor;
    private final TravelToolOrchestrator toolOrchestrator;
    private final SearchServiceClient searchServiceClient;
    private final BookingServiceClient bookingServiceClient;
    private final UserServiceClient userServiceClient;
    private static final Logger log =
            LoggerFactory.getLogger(TravelChatService.class);


    public TravelChatService(
            ConversationRepository conversationRepository,
            ConversationMemoryService memoryService,
            TravelAdvisorChain advisorChain,
            PromptBuilder promptBuilder,
            LlmService llmService, LiveSearchFormatter formatter, ConversationContextBuilder contextBuilder, PromptTokenLimiter tokenLimiter, IntentClassifier intentClassifier, TravelPlanner travelPlanner, ObjectMapper objectMapper, LiveSearchContextRedisService liveSearchContextRedisService, BookingSelectionService bookingSelectionService, PendingBookingRedisService pendingBookingRedisService, TravelPlanExtractor planExtractor, TravelToolOrchestrator toolOrchestrator, SearchServiceClient searchServiceClient, BookingServiceClient bookingServiceClient, UserServiceClient userServiceClient) {

        this.conversationRepository = conversationRepository;
        this.memoryService = memoryService;
        this.advisorChain = advisorChain;
        this.promptBuilder = promptBuilder;
        this.llmService = llmService;
        this.formatter = formatter;
        this.contextBuilder = contextBuilder;
        this.tokenLimiter = tokenLimiter;
        this.intentClassifier = intentClassifier;
        this.travelPlanner = travelPlanner;
        this.objectMapper = objectMapper;
        this.liveSearchContextRedisService = liveSearchContextRedisService;
        this.bookingSelectionService = bookingSelectionService;
        this.pendingBookingRedisService = pendingBookingRedisService;
        this.planExtractor = planExtractor;
        this.toolOrchestrator = toolOrchestrator;
        this.searchServiceClient = searchServiceClient;
        this.bookingServiceClient = bookingServiceClient;
        this.userServiceClient = userServiceClient;
    }

    /**
     * Create new conversation
     */

    // =====================================================
    // CREATE CONVERSATION
    // =====================================================

    public String chat(

            String userId,

            String conversationId,

            String question

    ) {

        log.info(
                "Chat request received. conversationId={}",
                conversationId
        );


        // =====================================================
        // 1. LOAD CONVERSATION HISTORY
        // =====================================================

        List<ConversationMessage> history =
                memoryService.load(
                        conversationId
                );


        // =====================================================
        // 2. CHECK BOOKING CONFIRMATION
        // =====================================================

        Optional<PendingBookingContext> pendingOptional =
                pendingBookingRedisService.find(conversationId);

        if (pendingOptional.isPresent()
                && isConfirmation(question)) {

            return handleBookingConfirmation(
                    userId,
                    conversationId,
                    question
            );
        }


        // =====================================================
        // 3. EXTRACT TRAVEL INTENT
        // =====================================================

        TravelPlan plan =
                planExtractor.extract(
                        question
                );

        log.info(
                "Extracted TravelPlan={}",
                plan
        );


        // =====================================================
        // 4. RETRIEVE STATIC KNOWLEDGE
        // =====================================================

        List<Document> documents =
                advisorChain.retrieveKnowledge(
                        question
                );


        // =====================================================
        // 5. BUILD COMPACT CONVERSATION CONTEXT
        // =====================================================

        String conversationContext =
                contextBuilder.build(
                        history
                );


        // =====================================================
        // 6. BUILD COMPACT KNOWLEDGE
        // =====================================================

        String knowledge =
                buildCompactKnowledge(
                        documents
                );


        // =====================================================
        // 7. CHECK FLIGHT/HOTEL SELECTION
        // =====================================================

        Optional<?> selectionResponse =
                handleSelection(
                        userId,
                        conversationId,
                        question
                );

        if (selectionResponse.isPresent()) {

            return (String)
                    selectionResponse.get();
        }


        // =====================================================
        // 8. EXECUTE LIVE TOOL
        // =====================================================

        ToolResult toolResult =
                null;

        if (plan.intent()
                != IntentType.GENERAL_CHAT) {

            toolResult =
                    toolOrchestrator.execute(

                            userId,

                            conversationId,

                            plan
                    );


            saveLiveSearchContext(
                    conversationId,
                    toolResult,
                    plan
            );
        }


        // =====================================================
        // 9. FORMAT LIVE SEARCH DATA
        // =====================================================

        String liveSearchSummary =
                buildLiveSearchSummary(
                        toolResult
                );


        // =====================================================
        // 10. BUILD PROMPT CONTEXT
        // =====================================================

        PromptContext context =
                new PromptContext(

                        plan,

                        conversationContext,

                        knowledge,

                        liveSearchSummary
                );


        // =====================================================
        // 11. BUILD FINAL PROMPT
        // =====================================================

        String finalPrompt =
                promptBuilder.buildPrompt(

                        question,

                        context
                );


        // =====================================================
        // 12. TOKEN LIMIT
        // =====================================================

        finalPrompt =
                tokenLimiter.limit(
                        finalPrompt
                );


        // =====================================================
        // 13. GENERATE AI RESPONSE
        // =====================================================

        String answer =
                llmService.chat(
                        finalPrompt
                );


        // =====================================================
        // 14. SAVE CONVERSATION
        // =====================================================

        saveConversationMessages(

                conversationId,

                question,

                answer
        );


        updateConversationTimestamp(
                conversationId
        );


        return answer;
    }


    // =====================================================
    // HANDLE USER SELECTION
    // =====================================================

    private Optional<String> handleSelection(

            String userId,

            String conversationId,

            String question

    ) {


        // =====================================================
        // FLIGHT SELECTION
        // =====================================================

        Optional<SelectedFlight> selectedFlight =
                bookingSelectionService.selectFlight(

                        userId,

                        conversationId,

                        question
                );


        if (selectedFlight.isPresent()) {

            SelectedFlight flight =
                    selectedFlight.get();


            String response =
                    """
                    You selected the following flight:

                    Flight: %s
                    Route: %s → %s
                    Departure: %s
                    Price: %s %s

                    Would you like to confirm this booking?
                    """
                            .formatted(

                                    flight.flightNumber(),

                                    flight.origin(),

                                    flight.destination(),

                                    flight.departure(),

                                    flight.price(),
                                    flight.currency()
                            );


            saveConversationMessages(

                    conversationId,

                    question,

                    response
            );


            return Optional.of(
                    response
            );
        }


        // =====================================================
        // HOTEL SELECTION
        // =====================================================

        Optional<SelectedHotel> selectedHotel =
                bookingSelectionService.selectHotel(

                        userId,

                        conversationId,

                        question
                );


        if (selectedHotel.isPresent()) {

            SelectedHotel hotel =
                    selectedHotel.get();


            String response =
                    """
                    You selected the following hotel:

                    Hotel: %s
                    Location: %s
                    Check-in: %s
                    Check-out: %s
                    Price: %s %s

                    Would you like to confirm this booking?
                    """
                            .formatted(

                                    hotel.hotelName(),

                                    hotel.city(),

                                    hotel.checkIn(),

                                    hotel.checkOut(),

                                    hotel.originalPrice(),

                                    hotel.currency()
                            );


            saveConversationMessages(

                    conversationId,

                    question,

                    response
            );


            return Optional.of(
                    response
            );
        }


        return Optional.empty();
    }


    // =====================================================
    // BOOKING CONFIRMATION
    // =====================================================

    private String handleBookingConfirmation(

            String userId,

            String conversationId,

            String question

    ) {

        // =====================================================
        // 1. FIND PENDING BOOKING
        // =====================================================

        Optional<PendingBookingContext> pendingOptional =
                pendingBookingRedisService.find(
                        conversationId
                );

        if (pendingOptional.isEmpty()) {

            String response =
                    """
                    I couldn't find a pending booking.
    
                    Please search and select a flight or hotel first.
                    """;

            saveConversationMessages(
                    conversationId,
                    question,
                    response
            );

            return response;
        }


        // =====================================================
        // 2. GET CURRENT PENDING BOOKING
        // =====================================================

        PendingBookingContext pending =
                pendingOptional.get();

        log.info(
                "Booking workflow state for conversation {}: {}",
                conversationId,
                pending.state()
        );


        // =====================================================
        // 3. PROCESS CURRENT WORKFLOW STATE
        // =====================================================

        return switch (pending.state()) {


            // =============================================
            // USER SELECTED FLIGHT/HOTEL
            // FIRST CONFIRMATION
            // =============================================

            case SELECTED -> processSelectedBooking(

                    userId,

                    conversationId,

                    question,

                    pending
            );


            // =============================================
            // REVALIDATION SUCCESSFUL
            // SECOND CONFIRMATION
            // =============================================

            case READY_FOR_BOOKING -> promptForPaymentMethod(

                    userId,

                    conversationId,

                    question,

                    pending
            );


            // =============================================
            // PAYMENT METHOD CHOSEN
            // =============================================

            case AWAITING_PAYMENT_METHOD -> handlePaymentMethodSelected(

                    userId,

                    conversationId,

                    question,

                    pending
            );


            // =============================================
            // PRICE CHANGED
            // =============================================

            case PRICE_CHANGED -> {

                String response =
                        """
                        The latest price has changed.
    
                        Please search and select the flight or hotel again
                        before continuing with the booking.
                        """;

                saveConversationMessages(
                        conversationId,
                        question,
                        response
                );

                yield response;
            }


            // =============================================
            // BOOKING ALREADY CREATED
            // =============================================

            case BOOKING_CREATED,
                 PAYMENT_PENDING -> {

                String response =
                        """
                        Your booking has already been created.
    
                        Payment processing is currently in progress.
    
                        Please do not submit the booking again.
                        """;

                saveConversationMessages(
                        conversationId,
                        question,
                        response
                );

                yield response;
            }


            // =============================================
            // BOOKING CONFIRMED
            // =============================================

            case CONFIRMED -> {

                String response =
                        """
                        Your booking has already been confirmed successfully.
                        """;

                saveConversationMessages(
                        conversationId,
                        question,
                        response
                );

                yield response;
            }


            // =============================================
            // BOOKING FAILED
            // =============================================

            case FAILED -> {

                String response =
                        """
                        The previous booking attempt failed.
    
                        Please search and select a new option.
                        """;

                saveConversationMessages(
                        conversationId,
                        question,
                        response
                );

                yield response;
            }


            // =============================================
            // PROCESSING STATES
            // =============================================

            case CONFIRMATION_RECEIVED,
                 REVALIDATING -> {

                String response =
                        """
                        Your booking request is currently being processed.
    
                        Please do not submit another confirmation.
                        """;

                saveConversationMessages(
                        conversationId,
                        question,
                        response
                );

                yield response;
            }
        };

    }

    private String processSelectedBooking(
            String userId,
            String conversationId,
            String question,
            PendingBookingContext pending
    ) {

        // =====================================================
        // 1. MARK CONFIRMATION RECEIVED
        // =====================================================

        pendingBookingRedisService.updateState(
                conversationId,
                BookingWorkflowState.CONFIRMATION_RECEIVED
        );


        // =====================================================
        // 2. MARK REVALIDATING
        // =====================================================

        pendingBookingRedisService.updateState(
                conversationId,
                BookingWorkflowState.REVALIDATING
        );


        // =====================================================
        // 3. FLIGHT
        // =====================================================

        if (pending.type()
                == PendingBookingType.FLIGHT) {

            return revalidateSelectedFlight(
                    userId,
                    conversationId,
                    question,
                    pending
            );
        }


        // =====================================================
        // 4. HOTEL
        // =====================================================

        if (pending.type()
                == PendingBookingType.HOTEL) {

            return revalidateSelectedHotel(
                    userId,
                    conversationId,
                    question,
                    pending
            );
        }


        return "Unsupported booking type.";
    }

    private String revalidateSelectedFlight(
            String userId,
            String conversationId,
            String question,
            PendingBookingContext pending
    ) {

        try {

            SelectedFlight flight =
                    objectMapper.readValue(
                            pending.selectedData(),
                            SelectedFlight.class
                    );


            FlightRevalidationRequest request =
                    new FlightRevalidationRequest(

                            userId,

                            flight.supplier(),

                            flight.flightId(),

                            flight.supplierOfferId(),

                            flight.flightNumber(),

                            flight.origin(),

                            flight.destination(),

                            flight.departure(),

                            flight.price(),

                            flight.currency(),

                            flight.passengers()
                    );


            FlightRevalidationResponse response =
                    searchServiceClient.revalidateFlight(
                            request
                    );


            // =================================================
            // NOT AVAILABLE
            // =================================================

            if (!response.available()) {

                pendingBookingRedisService.updateState(
                        conversationId,
                        BookingWorkflowState.FAILED
                );

                String message =
                        """
                        Sorry, this flight is no longer available.
    
                        %s
    
                        Please search for another flight.
                        """
                                .formatted(
                                        response.message()
                                );

                saveConversationMessages(
                        conversationId,
                        question,
                        message
                );

                return message;
            }


            // =================================================
            // PRICE CHANGED
            // =================================================

            if (response.priceChanged()) {

                pendingBookingRedisService.updateState(
                        conversationId,
                        PRICE_CHANGED
                );

                String message =
                        """
                        The flight is still available, but the price has changed.
    
                        Previous price: %s %s
                        Current price: %s %s
    
                        Please confirm the updated price before I create
                        the booking.
                        """
                                .formatted(
                                        response.originalPrice(),
                                        response.currency(),

                                        response.currentPrice(),
                                        response.currency()
                                );

                saveConversationMessages(
                        conversationId,
                        question,
                        message
                );

                return message;
            }


            // =================================================
            // REVALIDATION SUCCESS
            // =================================================

            pendingBookingRedisService.updateState(
                    conversationId,
                    BookingWorkflowState.READY_FOR_BOOKING
            );


            String message =
                    """
                    Your flight has been successfully revalidated.
    
                    Flight: %s
                    Current price: %s %s
    
                    The flight is available and the price has been verified.
    
                    Please confirm once more to create the booking.
                    """
                            .formatted(
                                    response.flightNumber(),
                                    response.currentPrice(),
                                    response.currency()
                            );


            saveConversationMessages(
                    conversationId,
                    question,
                    message
            );

            return message;

        } catch (Exception ex) {

            log.error(
                    "Flight revalidation failed",
                    ex
            );

            pendingBookingRedisService.updateState(
                    conversationId,
                    BookingWorkflowState.FAILED
            );

            return """
                Unable to revalidate the flight at this time.

                Please try again.
                """;
        }
    }
    // =====================================================
    // ASK FOR PAYMENT METHOD (new step, before creating the booking)
    // =====================================================

    private static final java.util.Set<String> SUPPORTED_PAYMENT_METHODS =
            java.util.Set.of("UPI", "CARD", "NETBANKING");

    private String promptForPaymentMethod(
            String userId,
            String conversationId,
            String question,
            PendingBookingContext pending
    ) {

        pendingBookingRedisService.updateState(
                conversationId,
                BookingWorkflowState.AWAITING_PAYMENT_METHOD
        );

        String message =
                """
                How would you like to pay?

                Please reply with one of: UPI, Card, NetBanking
                """;

        saveConversationMessages(
                conversationId,
                question,
                message
        );

        return message;
    }

    /**
     * Parses the user's free-text reply into one of SUPPORTED_PAYMENT_METHODS.
     * On a valid choice, saves it and proceeds to actually create the booking.
     * On an invalid/unrecognized reply, stays in AWAITING_PAYMENT_METHOD and
     * asks again instead of guessing.
     */
    private String handlePaymentMethodSelected(
            String userId,
            String conversationId,
            String question,
            PendingBookingContext pending
    ) {

        String normalized =
                question == null
                        ? ""
                        : question.trim().toUpperCase();

        String resolved = null;
        for (String candidate : SUPPORTED_PAYMENT_METHODS) {
            if (normalized.contains(candidate)) {
                resolved = candidate;
                break;
            }
        }
        if (normalized.contains("NET BANKING")) {
            resolved = "NETBANKING";
        }

        if (resolved == null) {

            String message =
                    """
                    Sorry, I didn't catch that.

                    Please reply with one of: UPI, Card, NetBanking
                    """;

            saveConversationMessages(
                    conversationId,
                    question,
                    message
            );

            return message;
        }

        pendingBookingRedisService.updatePaymentMethod(
                conversationId,
                resolved
        );

        PendingBookingContext updated =
                pendingBookingRedisService.find(conversationId)
                        .orElse(pending);

        return createActualBooking(
                userId,
                conversationId,
                question,
                updated
        );
    }

    private String createActualBooking(

            String userId,

            String conversationId,

            String question,

            PendingBookingContext pending

    ) {

        log.info(
                "Creating actual booking. conversationId={}, type={}",
                conversationId,
                pending.type()
        );


        // =====================================================
        // FLIGHT BOOKING
        // =====================================================

        if (pending.type()
                == PendingBookingType.FLIGHT) {

            return createFlightBooking(

                    userId,

                    conversationId,

                    question,

                    pending
            );
        }


        // =====================================================
        // HOTEL BOOKING
        // =====================================================

        if (pending.type()
                == PendingBookingType.HOTEL) {

            return createHotelBooking(

                    userId,

                    conversationId,

                    question,

                    pending
            );
        }


        // =====================================================
        // UNKNOWN TYPE
        // =====================================================

        String response =
                """
                Unsupported booking type.
                
                Please search and select a new travel option.
                """;

        saveConversationMessages(
                conversationId,
                question,
                response
        );

        return response;
    }

    private String revalidateSelectedHotel(
            String userId,
            String conversationId,
            String question,
            PendingBookingContext pending
    ) {

        try {

            SelectedHotel hotel =
                    objectMapper.readValue(
                            pending.selectedData(),
                            SelectedHotel.class
                    );


            HotelRevalidationRequest request =
                    new HotelRevalidationRequest(



                            hotel.hotelId(),

                            hotel.hotelName(),

                            hotel.city(),

                            hotel.checkIn(),

                            hotel.checkOut(),

                            hotel.adults(),

                            hotel.rooms(),

                            hotel.originalPrice(),

                            hotel.currency()
                    );


            HotelRevalidationResponse response =
                    searchServiceClient.revalidateHotel(
                            request
                    );


            // =============================================
            // HOTEL NOT AVAILABLE
            // =============================================

            if (!response.available()) {

                pendingBookingRedisService.updateState(
                        conversationId,
                        BookingWorkflowState.FAILED
                );

                String message =
                        """
                        Sorry, this hotel is no longer available.
    
                        %s
    
                        Please search for another hotel.
                        """
                                .formatted(
                                        response.message()
                                );

                saveConversationMessages(
                        conversationId,
                        question,
                        message
                );

                return message;
            }


            // =============================================
            // PRICE CHANGED
            // =============================================

            if (response.priceChanged()) {

                pendingBookingRedisService.updateState(
                        conversationId,
                        PRICE_CHANGED
                );

                String message =
                        """
                        The hotel is still available, but the price has changed.
    
                        Previous price: %s %s
                        Current price: %s %s
    
                        Please confirm the updated price before I create
                        the booking.
                        """
                                .formatted(
                                        response.originalPrice(),
                                        response.currency(),
                                        response.currentPrice(),
                                        response.currency()
                                );

                saveConversationMessages(
                        conversationId,
                        question,
                        message
                );

                return message;
            }


            // =============================================
            // REVALIDATION SUCCESS
            // =============================================

            pendingBookingRedisService.updateState(
                    conversationId,
                    BookingWorkflowState.READY_FOR_BOOKING
            );

            String message =
                    """
                    Your hotel has been successfully revalidated.
    
                    Hotel: %s
                    Current price: %s %s
    
                    The hotel is currently available.
    
                    Please confirm once more to create the booking.
                    """
                            .formatted(
                                    response.hotelName(),
                                    response.currentPrice(),
                                    response.currency()
                            );


            saveConversationMessages(
                    conversationId,
                    question,
                    message
            );

            return message;

        } catch (Exception ex) {

            log.error(
                    "Hotel revalidation failed",
                    ex
            );

            pendingBookingRedisService.updateState(
                    conversationId,
                    BookingWorkflowState.FAILED
            );

            return """
                Unable to revalidate the hotel at this time.

                Please try again.
                """;
        }
    }




    // =====================================================
    // FLIGHT REVALIDATION
    // =====================================================



    // =====================================================
    // HOTEL REVALIDATION
    // =====================================================

    private String createFlightBooking(

            String userId,

            String conversationId,

            String question,

            PendingBookingContext pending

    ) {

        try {

            // =====================================================
            // 1. UPDATE BOOKING STATE
            // =====================================================

            pendingBookingRedisService.updateState(
                    conversationId,
                    BookingWorkflowState.BOOKING_CREATED
            );


            // =====================================================
            // 2. READ SELECTED FLIGHT
            // =====================================================

            SelectedFlight flight =
                    objectMapper.readValue(

                            pending.selectedData(),

                            SelectedFlight.class
                    );


            // =====================================================
            // 3. GENERATE IDEMPOTENCY KEY
            // =====================================================

            String idempotencyKey =
                    UUID.randomUUID()
                            .toString();


            // =====================================================
            // 3b. RESOLVE PAYMENT METHOD + USER EMAIL
            // =====================================================

            String paymentMethod = pending.paymentMethod();
            if (paymentMethod == null || paymentMethod.isBlank()) {
                // Defensive: shouldn't happen since AWAITING_PAYMENT_METHOD
                // gates this step, but never silently default a real charge.
                pendingBookingRedisService.updateState(
                        conversationId,
                        BookingWorkflowState.FAILED
                );
                return """
                    Something went wrong before payment could be set up.

                    Please start the booking again.
                    """;
            }

            String userEmail = userServiceClient.getUserEmail(userId);
            if (userEmail == null || userEmail.isBlank()) {
                log.error(
                        "Could not resolve email for userId={}, aborting booking creation",
                        userId
                );
                pendingBookingRedisService.updateState(
                        conversationId,
                        BookingWorkflowState.FAILED
                );
                return """
                    We couldn't verify your account email, so the booking
                    could not be created.

                    Please try again or contact support.
                    """;
            }


            // =====================================================
            // 4. BUILD BOOKING REQUEST
            // =====================================================

            CreateFlightBookingRequest request =
                    new CreateFlightBookingRequest(

                            userId,

                            idempotencyKey,

                            flight.supplier(),

                            flight.flightId(),

                            flight.supplierOfferId(),

                            flight.flightNumber(),

                            flight.origin(),

                            flight.destination(),

                            flight.departure(),

                            flight.price(),

                            flight.currency(),

                            flight.passengers(),

                            paymentMethod,

                            userEmail
                    );


            log.info(
                    "Calling Booking Service for flight booking. " +
                            "conversationId={}, flightNumber={}",

                    conversationId,

                    flight.flightNumber()
            );


            // =====================================================
            // 5. CALL BOOKING SERVICE
            // =====================================================

            String bookingResponse =
                    bookingServiceClient.createFlightBooking(
                            request
                    );


            log.info(
                    "Flight booking created successfully. " +
                            "conversationId={}, response={}",

                    conversationId,

                    bookingResponse
            );


            // =====================================================
            // 6. SAVE BOOKING RESPONSE
            // =====================================================

            pendingBookingRedisService.updateBookingData(

                    conversationId,

                    bookingResponse
            );


            // =====================================================
            // 6b. LINK bookingUid -> conversationId
            // Needed so the Kafka listener (BookingConfirmedEvent /
            // BookingFailedEvent) knows which conversation to update
            // once payment resolves — those events only carry bookingUid.
            // =====================================================

            try {
                com.fasterxml.jackson.databind.JsonNode node =
                        objectMapper.readTree(bookingResponse);

                if (node.has("bookingUid")) {
                    pendingBookingRedisService.linkBookingToConversation(
                            node.get("bookingUid").asText(),
                            conversationId
                    );
                } else {
                    log.error(
                            "bookingResponse had no bookingUid field, cannot link for notifications: {}",
                            bookingResponse
                    );
                }
            } catch (Exception e) {
                log.error(
                        "Failed to parse bookingResponse to link bookingUid -> conversationId",
                        e
                );
            }


            // =====================================================
            // 7. UPDATE WORKFLOW STATE
            // =====================================================

            pendingBookingRedisService.updateState(

                    conversationId,

                    BookingWorkflowState.PAYMENT_PENDING
            );


            // =====================================================
            // 8. RESPONSE
            // =====================================================

            String response =
                    """
                    Great! Your flight booking has been created successfully.
    
                    Flight: %s
                    Route: %s → %s
                    Departure: %s
    
                    The booking is now awaiting payment.
                    Please proceed with the payment to confirm your booking.
                    """
                            .formatted(

                                    flight.flightNumber(),

                                    flight.origin(),

                                    flight.destination(),

                                    flight.departure()
                            );


            saveConversationMessages(

                    conversationId,

                    question,

                    response
            );


            return response;

        } catch (Exception ex) {

            log.error(

                    "Failed to create flight booking. conversationId={}",

                    conversationId,

                    ex
            );


            // =====================================================
            // MARK FAILED
            // =====================================================

            pendingBookingRedisService.updateState(

                    conversationId,

                    BookingWorkflowState.FAILED
            );


            String response =
                    """
                    Sorry, we were unable to create your flight booking.
    
                    Please try again.
                    """;


            saveConversationMessages(

                    conversationId,

                    question,

                    response
            );


            return response;
        }
    }

    private String createHotelBooking(

            String userId,

            String conversationId,

            String question,

            PendingBookingContext pending

    ) {

        try {

            // =====================================================
            // 1. UPDATE BOOKING STATE
            // =====================================================

            pendingBookingRedisService.updateState(

                    conversationId,

                    BookingWorkflowState.BOOKING_CREATED
            );


            // =====================================================
            // 2. READ SELECTED HOTEL
            // =====================================================

            SelectedHotel hotel =
                    objectMapper.readValue(

                            pending.selectedData(),

                            SelectedHotel.class
                    );


            // =====================================================
            // 3. GENERATE IDEMPOTENCY KEY
            // =====================================================

            String idempotencyKey =
                    UUID.randomUUID()
                            .toString();


            // =====================================================
            // 3b. RESOLVE PAYMENT METHOD + USER EMAIL
            // =====================================================

            String paymentMethod = pending.paymentMethod();
            if (paymentMethod == null || paymentMethod.isBlank()) {
                pendingBookingRedisService.updateState(
                        conversationId,
                        BookingWorkflowState.FAILED
                );
                return """
                    Something went wrong before payment could be set up.

                    Please start the booking again.
                    """;
            }

            String userEmail = userServiceClient.getUserEmail(userId);
            if (userEmail == null || userEmail.isBlank()) {
                log.error(
                        "Could not resolve email for userId={}, aborting hotel booking creation",
                        userId
                );
                pendingBookingRedisService.updateState(
                        conversationId,
                        BookingWorkflowState.FAILED
                );
                return """
                    We couldn't verify your account email, so the booking
                    could not be created.

                    Please try again or contact support.
                    """;
            }


            // =====================================================
            // 4. BUILD BOOKING REQUEST
            // =====================================================

            CreateHotelBookingRequest request =
                    new CreateHotelBookingRequest(

                            userId,

                            idempotencyKey,

                            hotel.supplier(),

                            hotel.hotelId(),

                            hotel.hotelName(),

                            hotel.city(),

                            hotel.checkIn(),

                            hotel.checkOut(),

                            hotel.originalPrice(),

                            hotel.currency(),

                            hotel.adults(),

                            hotel.rooms(),

                            paymentMethod,

                            userEmail
                    );


            log.info(
                    "Calling Booking Service for hotel booking. " +
                            "conversationId={}, hotel={}",

                    conversationId,

                    hotel.hotelName()
            );


            // =====================================================
            // 5. CALL BOOKING SERVICE
            // =====================================================

            String bookingResponse =
                    bookingServiceClient.createHotelBooking(
                            request
                    );


            log.info(
                    "Hotel booking created successfully. " +
                            "conversationId={}, response={}",

                    conversationId,

                    bookingResponse
            );


            // =====================================================
            // 6. SAVE BOOKING RESPONSE
            // =====================================================

            pendingBookingRedisService.updateBookingData(

                    conversationId,

                    bookingResponse
            );


            // =====================================================
            // 6b. LINK bookingUid -> conversationId (hotel bookings use
            // the same booking-confirmed-topic/booking-failed-topic +
            // BookingNotificationListener as flights)
            // =====================================================

            try {
                com.fasterxml.jackson.databind.JsonNode node =
                        objectMapper.readTree(bookingResponse);

                if (node.has("bookingUid")) {
                    pendingBookingRedisService.linkBookingToConversation(
                            node.get("bookingUid").asText(),
                            conversationId
                    );
                } else {
                    log.error(
                            "hotel bookingResponse had no bookingUid field, cannot link for notifications: {}",
                            bookingResponse
                    );
                }
            } catch (Exception e) {
                log.error(
                        "Failed to parse hotel bookingResponse to link bookingUid -> conversationId",
                        e
                );
            }


            // =====================================================
            // 7. UPDATE WORKFLOW STATE
            // =====================================================

            pendingBookingRedisService.updateState(

                    conversationId,

                    BookingWorkflowState.PAYMENT_PENDING
            );


            // =====================================================
            // 8. RESPONSE
            // =====================================================

            String response =
                    """
                    Great! Your hotel booking has been created successfully.
    
                    Hotel: %s
                    Location: %s
                    Check-in: %s
                    Check-out: %s
    
                    The booking is now awaiting payment.
                    Please proceed with the payment to confirm your booking.
                    """
                            .formatted(

                                    hotel.hotelName(),

                                    hotel.city(),

                                    hotel.checkIn(),

                                    hotel.checkOut()
                            );


            saveConversationMessages(

                    conversationId,

                    question,

                    response
            );


            return response;

        } catch (Exception ex) {

            log.error(

                    "Failed to create hotel booking. conversationId={}",

                    conversationId,

                    ex
            );


            // =====================================================
            // MARK FAILED
            // =====================================================

            pendingBookingRedisService.updateState(

                    conversationId,

                    BookingWorkflowState.FAILED
            );


            String response =
                    """
                    Sorry, we were unable to create your hotel booking.
    
                    Please try again.
                    """;


            saveConversationMessages(

                    conversationId,

                    question,

                    response
            );


            return response;
        }
    }




    // =====================================================
    // BUILD LIVE SEARCH SUMMARY
    // =====================================================

    private String buildLiveSearchSummary(
            ToolResult toolResult
    ) {

        if (toolResult == null) {

            return "No live search was performed.";
        }


        if (!toolResult.success()) {

            return "LIVE SEARCH FAILED: "
                    + toolResult.message();
        }


        if (toolResult.message() == null
                || toolResult.message().isBlank()) {

            return "LIVE SEARCH RETURNED NO RESULTS.";
        }


        return switch (
                toolResult.toolType()
                ) {

            case HOTEL_SEARCH ->
                    formatter.formatHotelSearch(
                            toolResult.message()
                    );

            case FLIGHT_SEARCH ->
                    formatter.formatFlightSearch(
                            toolResult.message()
                    );

            default ->
                    toolResult.message();
        };
    }


    // =====================================================
    // BUILD COMPACT KNOWLEDGE
    // =====================================================

    private String buildCompactKnowledge(
            List<Document> documents
    ) {

        if (documents == null
                || documents.isEmpty()) {

            return "No relevant static travel knowledge found.";
        }


        StringBuilder knowledge =
                new StringBuilder();


        int limit =
                Math.min(
                        3,
                        documents.size()
                );


        for (int i = 0;
             i < limit;
             i++) {

            Document document =
                    documents.get(i);


            if (document == null
                    || document.getText() == null
                    || document.getText().isBlank()) {

                continue;
            }


            knowledge.append(
                    document.getText()
            );

            knowledge.append(
                    "\n\n"
            );
        }


        return knowledge
                .toString()
                .trim();
    }


    // =====================================================
    // SAVE LIVE SEARCH CONTEXT
    // =====================================================

    private void saveLiveSearchContext(

            String conversationId,

            ToolResult toolResult,

            TravelPlan plan

    ) {

        if (toolResult == null
                || !toolResult.success()
                || toolResult.message() == null
                || toolResult.message().isBlank()) {

            return;
        }


        try {

            switch (
                    toolResult.toolType()
            ) {

                case FLIGHT_SEARCH ->

                        liveSearchContextRedisService.save(

                                conversationId,

                                PendingBookingType.FLIGHT,

                                objectMapper.readTree(
                                        toolResult.message()
                                )
                        );


                case HOTEL_SEARCH ->

                        liveSearchContextRedisService.save(

                                conversationId,

                                PendingBookingType.HOTEL,

                                enrichHotelResults(
                                        objectMapper.readTree(
                                                toolResult.message()
                                        ),
                                        plan
                                )
                        );


                default -> {

                    // No booking context required
                }
            }

        } catch (Exception ex) {

            log.error(
                    "Failed to save live search context",
                    ex
            );
        }
    }

    /**
     * Search Service's HotelInventoryDTO response has no checkIn/
     * checkOut/adults/rooms per hotel (those were search REQUEST
     * params, never part of each hotel's response) and its price
     * field is named "price", not "originalPrice". HotelOption (used
     * by BookingSelectionService.selectHotel via a straight Jackson
     * deserialize) expects checkIn/checkOut/originalPrice/adults/rooms
     * to exist — without this enrichment, every one of those silently
     * comes back null (Jackson doesn't error on missing fields, it
     * just leaves them null), which is exactly the "Check-in: null,
     * Price: null" bug.
     */
    private com.fasterxml.jackson.databind.JsonNode enrichHotelResults(
            com.fasterxml.jackson.databind.JsonNode hotelsNode,
            TravelPlan plan
    ) {

        if (hotelsNode == null || !hotelsNode.isArray()) {
            return hotelsNode;
        }

        for (com.fasterxml.jackson.databind.JsonNode hotel : hotelsNode) {

            if (!(hotel instanceof com.fasterxml.jackson.databind.node.ObjectNode obj)) {
                continue;
            }

            if (plan.travelDate() != null) {
                obj.put("checkIn", plan.travelDate().toString());
            }
            if (plan.returnDate() != null) {
                obj.put("checkOut", plan.returnDate().toString());
            }
            if (plan.adults() != null) {
                obj.put("adults", plan.adults());
            }
            if (plan.rooms() != null) {
                obj.put("rooms", plan.rooms());
            }

            // HotelOption expects "originalPrice"; the search response
            // field is named "price" — copy it across rather than
            // renaming the source field, in case anything else in the
            // pipeline still reads "price" from this same JSON.
            if (obj.has("price")) {
                obj.set("originalPrice", obj.get("price"));
            }
        }

        return hotelsNode;
    }


    // =====================================================
    // CONFIRMATION DETECTION
    // =====================================================

    private boolean isConfirmation(
            String question
    ) {

        if (question == null
                || question.isBlank()) {

            return false;
        }


        String normalized =
                question

                        .trim()

                        .toLowerCase(
                                Locale.ROOT
                        );


        return normalized.equals("yes")
                || normalized.equals("yes, confirm")
                || normalized.equals("confirm")
                || normalized.equals("confirm booking")
                || normalized.equals("book it")
                || normalized.equals("book now")
                || normalized.equals("proceed")
                || normalized.equals("proceed with booking")
                || normalized.equals("continue")
                || normalized.equals("pay now")
                || normalized.equals("make payment")
                || normalized.equals("go ahead");
    }


    // =====================================================
    // SAVE CONVERSATION
    // =====================================================

    private void saveConversationMessages(

            String conversationId,

            String question,

            String response

    ) {

        memoryService.addUserMessage(

                conversationId,

                question
        );


        memoryService.addAssistantMessage(

                conversationId,

                response
        );


        updateConversationTimestamp(
                conversationId
        );
    }


    // =====================================================
    // UPDATE CONVERSATION
    // =====================================================

    private void updateConversationTimestamp(
            String conversationId
    ) {

        conversationRepository
                .findById(
                        conversationId
                )
                .ifPresent(
                        conversation -> {

                            conversation.setUpdatedAt(
                                    LocalDateTime.now()
                            );

                            conversationRepository.save(
                                    conversation
                            );
                        }
                );
    }


    // =====================================================
    // GET HISTORY
    // =====================================================

    public List<ConversationMessage> history(
            String conversationId
    ) {

        return memoryService.load(
                conversationId
        );
    }


    // =====================================================
    // CLEAR CONVERSATION
    // =====================================================

    public void clearConversation(
            String conversationId
    ) {

        memoryService.clear(
                conversationId
        );

        pendingBookingRedisService.delete(
                conversationId
        );
    }


    // =====================================================
    // DELETE CONVERSATION
    // =====================================================

    public void deleteConversation(
            String conversationId
    ) {

        memoryService.delete(
                conversationId
        );

        pendingBookingRedisService.delete(
                conversationId
        );

        conversationRepository.deleteById(
                conversationId
        );
    }
}





