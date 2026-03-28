package com.example.url_system.dtos.mappers;


import com.example.url_system.dtos.chat.ChatMessageRetrieveResponse;
import com.example.url_system.models.ChatMessage;

import java.util.List;

public interface ChatMessageMapper {
    List<ChatMessageRetrieveResponse> toChatMessageResponseListDto(List<ChatMessage> messages);
}
