package org.example.common.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PriceWatchRequest {

    private String bookingId;
    private String userId;
    private String flightNumber;
    private double originalPrice;
    private String source;
    private String destination;
    private String travelDate;
}
