package com.example.notificationservice.consumer;

import jakarta.mail.internet.MimeMessage;
import org.example.common.dto.DelayDetectedEvent;
import org.example.common.dto.InvoiceGeneratedEvent;
import org.example.common.dto.PriceDropDetectedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Optional;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final Logger log = LoggerFactory.getLogger(getClass());

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendInvoiceEmail(InvoiceGeneratedEvent event) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true);

            helper.setFrom("adhikarybiswajit.techno@gmail.com");
            helper.setTo(event.userEmail());
            helper.setSubject("🧾 Your Flight Booking Invoice");

            helper.setText("""
                    Dear Customer,

                    Your booking invoice is attached.

                    Invoice ID: %s
                    Booking ID: %s

                    Thank you for choosing us!
                    """.formatted(
                    event.invoiceId(),
                    event.bookingUid()
            ));

            // 📎 ATTACH PDF
            FileSystemResource file =
                    new FileSystemResource(new File(event.pdfPath()));

            helper.addAttachment("Flight-Invoice.pdf", file);

            mailSender.send(message);

            log.info("📧 Invoice email with attachment sent to {}", event.userEmail());

        } catch (Exception e) {
            log.error("❌ Failed to send invoice email", e);
        }
    }

    public void sendPriceDropEmail(PriceDropDetectedEvent event) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false);

            helper.setFrom("adhikarybiswajit.techno@gmail.com");
            helper.setTo(event.getUserEmail());
            helper.setSubject("💸 Price Drop Alert!");

            helper.setText("""
                Good news!

                The price for your watched flight has dropped.

                ✈ Flight: %s
                Old Price: ₹%.2f
                New Price: ₹%.2f

                Book now before it increases again!
                """.formatted(
                    event.getFlightId(),
                    event.getTargetPrice().doubleValue(),
                    event.getLivePrice().doubleValue()
            ));

            mailSender.send(message);

            log.info("📧 Price drop alert sent to {}", event.getUserEmail());

        } catch (Exception e) {
            log.error("❌ Failed to send price drop email", e);
        }
    }
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom("adhikarybiswajit.techno@gmail.com");
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
            log.info("✅ Email sent successfully to {}", to);
        } catch (MailException e) {
            log.error("❌ Failed to send email to {}: {}", to, e.getMessage());
            throw e;  // ✅ re-throw so the caller knows it failed
        }
    }

}
