package com.example.chatbotservice.rag.planner;

import com.example.chatbotservice.rag.planner.service.PlannerResponse;

import com.example.chatbotservice.rag.planner.service.TravelPlan;
import com.example.chatbotservice.rag.registry.ToolRegistry;
import com.example.chatbotservice.rag.tool.ToolResult;
import com.example.chatbotservice.rag.tool.ToolType;
import com.example.chatbotservice.rag.tool.TravelTool;
import com.example.chatbotservice.service.TravelToolOrchestrator;
import org.springframework.stereotype.Component;

@Component
public class TravelPlanner {

    private final TravelToolOrchestrator toolOrchestrator;

    public TravelPlanner(
            TravelToolOrchestrator toolOrchestrator) {

        this.toolOrchestrator = toolOrchestrator;
    }

    public ToolResult execute(
            String userId,
            String conversationId,
            TravelPlan plan) {

        return toolOrchestrator.execute(
                userId,
                conversationId,
                plan
        );
    }
}