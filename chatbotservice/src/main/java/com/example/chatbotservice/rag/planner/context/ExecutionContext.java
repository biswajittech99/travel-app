package com.example.chatbotservice.rag.planner.context;

import com.example.chatbotservice.rag.tool.ToolResult;

import java.util.ArrayList;
import java.util.List;

public class ExecutionContext {
    private final List<ToolResult> results =
            new ArrayList<>();

    public void add(ToolResult result) {

        results.add(result);

    }

    public List<ToolResult> results() {

        return results;

    }
}
