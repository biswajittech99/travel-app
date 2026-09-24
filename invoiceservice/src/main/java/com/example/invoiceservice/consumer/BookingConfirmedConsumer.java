package com.example.invoiceservice.consumer;

import com.example.invoiceservice.service.InvoiceService;
import org.example.common.dto.BookingConfirmedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class BookingConfirmedConsumer {
    private final InvoiceService invoiceService;
    private final Logger log = LoggerFactory.getLogger(getClass());

    public BookingConfirmedConsumer(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @KafkaListener(
            topics = "booking-confirmed-topic",
            groupId = "invoice-service-group"
    )
    public void consume(BookingConfirmedEvent event)
    {
        try {
            log.info("🧾 Received BookingConfirmedEvent: {}", event);

            invoiceService.generateInvoice(event);
        }catch(Exception e)
        {

            log.error("Invoice generation failed for booking {}", event.bookingUid(), e);
        }
    }
}
