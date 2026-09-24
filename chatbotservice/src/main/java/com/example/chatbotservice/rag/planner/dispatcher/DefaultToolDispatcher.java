///package com.example.chatbotservice.rag.planner.dispatcher;

import com.example.chatbotservice.rag.planner.model.ExecutionPlan;
import com.example.chatbotservice.rag.planner.service.TravelPlan;
import com.example.chatbotservice.rag.tool.ToolResult;
import com.example.chatbotservice.rag.tool.ToolType;
import com.example.chatbotservice.rag.tool.TravelTool;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//@Service
//public class DefaultToolDispatcher implements ToolDispatcher{
//    private final Map<ToolType, TravelTool> toolRegistry;
//
//    public DefaultToolDispatcher(List<TravelTool> tools) {
//
//        this.toolRegistry =
//
//                tools.stream()
//
//                        .collect(
//
//                                Collectors.toMap(
//
//                                        TravelTool::type,
//
//                                        t -> t
//
//                                )
//
//                        );
//    }
//
//
//    @Override
//    public List<ToolResult> execute(String userId, String conversationId, ExecutionPlan plan, String question) {
//        List<ToolResult> results = new ArrayList<>();
//
//        for (ToolType tool : plan.tools()) {
//
//            TravelTool t = toolRegistry.get(tool);
//
//            if (t == null) {
//                continue;
//            }
//
//            results.add(
//
//                    t.execute(
//
//                            userId,
//
//                            conversationId,
//
//
//
//                    )
//
//            );
//        }
//
//        return results;
//    }
//
//
//}
