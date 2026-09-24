package com.example.chatbotservice.booking;

import com.example.chatbotservice.rag.dto.HotelOption;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class HotelSelectionService {

    /**
     * Detects hotel selection:
     *
     * "Book the first hotel"
     * "Select option 2"
     * "I want the third hotel"
     */
    public Optional<Integer> detectSelection(
            String question,
            List<HotelOption> hotels
    ) {

        if (hotels == null || hotels.isEmpty()) {

            return Optional.empty();
        }

        String text =
                question.toLowerCase(Locale.ROOT);

        if (text.contains("first") ||
                text.contains("hotel 1") ||
                text.contains("option 1")) {

            return Optional.of(0);
        }

        if (text.contains("second") ||
                text.contains("hotel 2") ||
                text.contains("option 2")) {

            return hotels.size() > 1
                    ? Optional.of(1)
                    : Optional.empty();
        }

        if (text.contains("third") ||
                text.contains("hotel 3") ||
                text.contains("option 3")) {

            return hotels.size() > 2
                    ? Optional.of(2)
                    : Optional.empty();
        }

        return Optional.empty();
    }
}
