package com.example.chatbotservice.booking;

import com.example.chatbotservice.rag.dto.TravelOption;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class FlightSelectionService {

    /**
     * Detects flight selection such as:
     *
     * "Book the first flight"
     * "Select flight 2"
     * "I want the third option"
     */
    public Optional<Integer> detectSelection(
            String question,
            List<TravelOption> flights
    ) {

        if (flights == null || flights.isEmpty()) {

            return Optional.empty();
        }

        String text =
                question.toLowerCase(Locale.ROOT);

        if (text.contains("first") ||
                text.contains("flight 1") ||
                text.contains("option 1")) {

            return Optional.of(0);
        }

        if (text.contains("second") ||
                text.contains("flight 2") ||
                text.contains("option 2")) {

            return flights.size() > 1
                    ? Optional.of(1)
                    : Optional.empty();
        }

        if (text.contains("third") ||
                text.contains("flight 3") ||
                text.contains("option 3")) {

            return flights.size() > 2
                    ? Optional.of(2)
                    : Optional.empty();
        }

        return Optional.empty();
    }
}
