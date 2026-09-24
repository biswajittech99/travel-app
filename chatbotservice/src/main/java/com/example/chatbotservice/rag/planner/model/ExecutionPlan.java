package com.example.chatbotservice.rag.planner.model;

import com.example.chatbotservice.rag.planner.IntentType;
import com.example.chatbotservice.rag.tool.ToolType;

import java.util.List;
import java.util.Map;

public record ExecutionPlan(IntentType intent,

                            List<ToolType> tools,

                            Map<String, Object> parameters) {
}
