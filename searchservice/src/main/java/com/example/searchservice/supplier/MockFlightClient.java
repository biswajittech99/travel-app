//package com.example.searchservice.supplier;
//
//import com.example.searchservice.dto.TravelOption;
//import com.example.searchservice.enums.TravelType;
//import com.example.searchservice.request.TravelSearchRequest;
//import org.springframework.context.annotation.Profile;
//import reactor.core.publisher.Mono;
//import org.springframework.stereotype.Component;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.time.ZoneOffset;
//import java.time.ZonedDateTime;
//import java.util.List;
//import java.util.UUID;
//
//@Component
//@Profile("mock")
//public class MockFlightClient implements SupplierClient{
//    @Override
//    public TravelType supportedType() {
//        return TravelType.FLIGHT;
//    }
//
//    @Override
//    public Mono<List<TravelOption>> search(TravelSearchRequest request) {
//
//        LocalDate date = request.departureDate();
//
//        TravelOption flight1 = new TravelOption(
//                "MockAir",
//                UUID.randomUUID().toString(),
//                request.origin(),
//                request.destination(),
//                "MA101",
//                "ECONOMY",
//                date.atTime(10, 30)
//                        .atZone(ZoneOffset.UTC)
//                        .toInstant(),
//                date.atTime(12, 45)
//                        .atZone(ZoneOffset.UTC)
//                        .toInstant(),
//                BigDecimal.valueOf(5500),
//                "INR",
//                45
//        );
//
//        TravelOption flight2 = new TravelOption(
//                "Demo Airways",
//                UUID.randomUUID().toString(),
//                request.origin(),
//                request.destination(),
//                "DA205",
//                "ECONOMY",
//                date.atTime(14, 0)
//                        .atZone(ZoneOffset.UTC)
//                        .toInstant(),
//                date.atTime(16, 20)
//                        .atZone(ZoneOffset.UTC)
//                        .toInstant(),
//                BigDecimal.valueOf(6200),
//                "INR",
//                32
//        );
//
//        TravelOption flight3 = new TravelOption(
//                "Test Airlines",
//                UUID.randomUUID().toString(),
//                request.origin(),
//                request.destination(),
//                "TA309",
//                "ECONOMY",
//                date.atTime(18, 30)
//                        .atZone(ZoneOffset.UTC)
//                        .toInstant(),
//                date.atTime(20, 50)
//                        .atZone(ZoneOffset.UTC)
//                        .toInstant(),
//                BigDecimal.valueOf(4800),
//                "INR",
//                18
//        );
//
//        return Mono.just(
//                List.of(
//                        flight1,
//                        flight2,
//                        flight3
//                )
//        );
//    }
//    }
