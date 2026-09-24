package com.example.chatbotservice.rag.registry;


import com.example.chatbotservice.rag.tool.TravelTool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ToolRegistry {

    private final List<TravelTool> tools;

    public ToolRegistry(List<TravelTool> tools) {
        this.tools = tools;
    }

    public List<TravelTool> getTools() {
        return tools;
    }
}
