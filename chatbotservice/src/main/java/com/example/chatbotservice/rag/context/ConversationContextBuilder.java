package com.example.chatbotservice.rag.context;

import com.example.chatbotservice.rag.memory.ConversationMessage;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ConversationContextBuilder {


    private static final int MAX_MESSAGES = 6;

    public String build(List<ConversationMessage> history){

        if(history==null || history.isEmpty()){
            return "No previous conversation.";
        }

        int start=Math.max(0,history.size()-MAX_MESSAGES);

        StringBuilder sb=new StringBuilder();

        history.subList(start,history.size())

                .forEach(message->{

                    sb.append(
                            message.getRole().equals("user")
                                    ? "User: "
                                    : "Assistant: "
                    );

                    sb.append(message.getContent());

                    sb.append("\n");

                });

        return sb.toString();

    }
}
