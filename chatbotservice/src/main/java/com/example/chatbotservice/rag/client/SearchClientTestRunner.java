//package com.example.chatbotservice.rag.client;

import com.example.chatbotservice.rag.dto.FlightSearchRequest;
import com.example.chatbotservice.rag.dto.TravelOption;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

//@Component
//public class SearchClientTestRunner implements CommandLineRunner {
//
//    private final SearchServiceClient client;
//
//    public SearchClientTestRunner(
//            SearchServiceClient client) {
//
//        this.client = client;
//    }
//    @Override
//    public void run(String... args) throws Exception {
//
//        List<TravelOption> flights =
//                client.searchFlights(
//
//                        new FlightSearchRequest(
//
//                                "DEL",
//
//                                "CCU",
//
//                                "2026-08-31"));
//
//        System.out.println(flights);
//
//    }
//
//
//}
