package com.example.url_system.dtos.mappers;


import com.example.url_system.dtos.chat.ChatMessageResponse;
import com.example.url_system.dtos.chat.ChatMessageRetrieveResponse;
import com.example.url_system.models.ChatMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ChatMessageImpl implements ChatMessageMapper{
    @Override
    public List<ChatMessageRetrieveResponse> toChatMessageResponseListDto(List<ChatMessage> messages) {
        if (messages == null) {
            return null;
        }

        List<ChatMessageRetrieveResponse> mappedMessages = new ArrayList<>();

        for (ChatMessage mes : messages) {
            if (mes == null) {
                return null;
            }

            mappedMessages.add(
                    new ChatMessageRetrieveResponse(
                            mes.getId(),
                            mes.getConversation().getId(),
                            mes.getContent(),
                            mes.getSenderType()
                    )
            );
        }

        return mappedMessages;
    }
}
