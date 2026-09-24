package com.example.chatbotservice.rag.tool;

import com.example.chatbotservice.rag.planner.service.TravelPlan;

public interface TravelTool {

    ToolType type();

    boolean supports(TravelPlan plan);

    ToolResult execute(
            String userId,
            String conversationId,
            TravelPlan plan
    );

}
