package com.example.chatbotservice.rag.tool;

import java.util.Map;

public record ToolResult(boolean success,

                         ToolType toolType,

                         String message,

                         Map<String, Object> data) {

    public static ToolResult success(
            ToolType toolType,
            String message,
            Map<String, Object> data
    ) {
        return new ToolResult(
                true,
                toolType,
                message,
                data
        );
    }

    public static ToolResult failure(
            ToolType toolType,
            String message
    ) {
        return new ToolResult(
                false,
                toolType,
                message,
                Map.of()
        );
    }
}
