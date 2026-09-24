package com.example.chatbotservice.rag.planner.dispatcher;

import com.example.chatbotservice.rag.planner.model.ExecutionPlan;
import com.example.chatbotservice.rag.tool.ToolResult;

import java.util.List;

public interface ToolDispatcher {

    List<ToolResult> execute(
            String userId,
            String conversationId,
            ExecutionPlan plan,
            String question
    );
}
