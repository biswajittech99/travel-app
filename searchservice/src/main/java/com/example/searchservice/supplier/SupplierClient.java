package com.example.searchservice.supplier;

import com.example.searchservice.dto.SearchRequest;
import com.example.searchservice.dto.TravelOption;
import com.example.searchservice.enums.TravelType;
import com.example.searchservice.request.TravelSearchRequest;
import reactor.core.publisher.Mono;

import java.util.List;

public interface SupplierClient {
    TravelType supportedType();

    Mono<List<TravelOption>> search(
            TravelSearchRequest request);

}
