package com.example.chatbotservice.rag.llm;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class SpringAiLlmService implements LlmService{

    private final ChatClient chatClient;


    public SpringAiLlmService(ChatClient.Builder builder) {

        this.chatClient = builder.build();

    }


    @Override
    public String chat(String prompt) {

        return chatClient
                .prompt()
                .user(prompt)
                .call()
                .content();
    }

    @Override
    public LlmResponse chat(LlmRequest request) {
        return null;
    }
}
