package com.example.chatbotservice.rag.context;

import com.example.chatbotservice.rag.planner.service.TravelPlan;

public record PromptContext(TravelPlan travelPlan,

                            String conversationHistory,

                            String knowledge,

                            String liveSearchSummary) {
}
