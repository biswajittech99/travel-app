package com.example.chatbotservice.rag.tool;

import com.example.chatbotservice.rag.planner.service.TravelPlan;
import com.example.chatbotservice.service.ToolExecutorService;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class BookingHistoryTool implements TravelTool{

    @Override
    public ToolType type() {
        return null;
    }

    @Override
    public boolean supports(TravelPlan plan) {
        return false;
    }

    @Override
    public ToolResult execute(String userId, String conversationId, TravelPlan plan) {
        return null;
    }
}



