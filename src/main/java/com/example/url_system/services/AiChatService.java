package com.example.url_system.services;

import com.example.url_system.models.ChatMessage;
import com.example.url_system.models.SenderType;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AiChatService {

    private final ChatClient chatClient;

    public AiChatService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }


    public String generateResponse(List<ChatMessage> conversationHistory, String userMessage) {
        List<Message> messages = new ArrayList<>();

        messages.add(new SystemMessage(
                "You are an AI assistant for the url-system application. " +
                        "Help users operate the app. " +
                        "Use the previous conversation context when answering. " +
                        "If the user asks to explain better, improve your previous explanation. " +
                        "Be concise, clear, and practical." +
                        "do not invent buttons/pages" +
                        "give step-by-step instruction"
        ));

        if (conversationHistory != null && !conversationHistory.isEmpty()) {
            for (ChatMessage chatMessage : conversationHistory) {
                if (chatMessage.getSenderType() == SenderType.USER) {
                    messages.add(new UserMessage(chatMessage.getContent()));
                } else if (chatMessage.getSenderType() == SenderType.ASSISTANT) {
                    messages.add(new AssistantMessage(chatMessage.getContent()));
                }
            }
        }

        messages.add(new UserMessage(userMessage));

        return chatClient.prompt()
                .messages(messages)
                .call()
                .content();
    }
}
