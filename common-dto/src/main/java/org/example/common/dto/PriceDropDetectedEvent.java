package org.example.common.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceDropDetectedEvent {
    private String alertId;
    private String userId;
    private String userEmail;
    private String flightId;
    private BigDecimal targetPrice;
    private BigDecimal livePrice;

}
