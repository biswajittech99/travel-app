package org.example.common.dto;

import java.time.Instant;
import java.util.List;

public record DelayDetectedEvent( String flightId,
                                  String bookingUid,
                                  String userId,
                                  String userEmail,
                                  Integer delayMinutes,
                                  Instant detectedAt
                                  ) {
}
