package org.example.common.dto;

public record InvoiceGeneratedEvent(String invoiceId,
                                    String bookingUid,
                                    String userEmail,
                                    String pdfPath) {
}
