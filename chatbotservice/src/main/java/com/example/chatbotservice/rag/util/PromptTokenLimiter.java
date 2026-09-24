package com.example.chatbotservice.rag.util;

import org.springframework.stereotype.Component;

@Component
public class PromptTokenLimiter {

    private static final int MAX_CHARACTERS = 14000;

    public String limit(String prompt){

        if(prompt==null){
            return "";
        }

        if(prompt.length()<=MAX_CHARACTERS){
            return prompt;
        }

        return prompt.substring(0,MAX_CHARACTERS)
                + "\n\n[Prompt truncated because it exceeded maximum size.]";

    }
}
