package org.example.common.dto;

import java.math.BigDecimal;

public record DelayClaimRequest(String bookingUid,
                                String flightId,
                                Integer delayMinutes,
                                String userId,
                                String userEmail,
                                BigDecimal claimAmount) {
}
