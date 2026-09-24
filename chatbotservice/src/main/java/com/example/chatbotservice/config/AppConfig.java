//package com.example.chatbotservice.config;
//
//import com.example.chatbotservice.service.ChatService;
//import com.example.chatbotservice.service.RawApiChatService;
//import com.example.chatbotservice.service.SpringAiChatService;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//
//@Configuration
//public class AppConfig {
//
//    @Value("${chatbot.mode}")
//    private String chatbotMode;
//
//    @Bean
//    @Primary
//    public ChatService chatService(
//            RawApiChatService rawApiChatService,
//            SpringAiChatService springAiChatService) {
//
//        if ("spring-ai".equals(chatbotMode)) {
//            System.out.println("✅ Using Spring AI implementation");
//            return springAiChatService;
//        } else {
//            System.out.println("✅ Using Raw API implementation");
//            return rawApiChatService;
//        }
//    }
//}
