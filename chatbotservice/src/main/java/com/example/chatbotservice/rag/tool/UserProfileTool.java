package com.example.chatbotservice.rag.tool;

//import com.example.chatbotservice.rag.planner.service.TravelPlan;
//import com.example.chatbotservice.service.ToolExecutorService;
//import org.springframework.stereotype.Component;
//
//import java.util.Map;
//
//@Component
//public class UserProfileTool implements TravelTool{
//
//    private final ToolExecutorService executor;
//
//    public UserProfileTool(
//            ToolExecutorService executor) {
//
//        this.executor = executor;
//    }
//
//
//    @Override
//    public ToolType type() {
//       return  ToolType.USER_PROFILE;
//    }
//
//    @Override
//    public boolean supports(TravelPlan plan) {
//        String q = question.toLowerCase();
//
//        return q.contains("my profile")
//                || q.contains("my account")
//                || q.contains("my information");
//    }
//
//    @Override
//    public ToolResult execute(String userId, String conversationId, TravelPlan plan) {
//        return executor.execute(
//                "get_user_profile",
//                Map.of(
//                        "user_id",
//                        userId),
//                userId);
//    }
//
//
//}
