package org.example.common.dto;


import lombok.Data;

@Data
public class PriceAlertRequest {
    public String searchKey;               // e.g. DEL-BLR-2025-02-10
    public String flightId;                // e.g. AI-203
    public double targetPrice;             // alert trigger price
    public boolean autoRebook;             // future use
    public String preferredPaymentMethod;  // UPI / CARD
    public String userEmail;               // notification email
}
