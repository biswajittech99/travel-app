package com.example.chatbotservice.rag.parsar;

import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DateParser {
    public LocalDate parse(String text) {

        String q = text.toLowerCase();

        if (q.contains("today")) {
            return LocalDate.now();
        }

        if (q.contains("tomorrow")) {
            return LocalDate.now().plusDays(1);
        }

        if (q.contains("day after tomorrow")) {
            return LocalDate.now().plusDays(2);
        }

        return LocalDate.now().plusDays(1);

    }

}
