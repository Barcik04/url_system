package com.example.url_system.dtos.chat;

public record ChatMessageResponse(
        Long messageId,
        Long conversationId,
        String userMessage,
        String assistantMessage
) {
}
