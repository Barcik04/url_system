package com.example.url_system.dtos.chat;

import com.example.url_system.models.SenderType;

public record ChatMessageRetrieveResponse(
        Long messageId,
        Long conversationId,
        String message,
        SenderType senderType
) {
}
