package com.example.url_system.services;

import com.example.url_system.dtos.chat.ChatMessageResponse;
import com.example.url_system.dtos.chat.ChatMessageRetrieveResponse;
import com.example.url_system.dtos.mappers.ChatMessageMapper;
import com.example.url_system.models.ChatConversation;
import com.example.url_system.models.ChatMessage;
import com.example.url_system.models.SenderType;
import com.example.url_system.repositories.ChatConversationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ChatConversationService {
    private final ChatConversationRepository chatConversationRepository;
    private final AiChatService aiChatService;
    private final ChatMessageMapper chatMessageMapper;

    public ChatConversationService(ChatConversationRepository chatConversationRepository, AiChatService aiChatService, ChatMessageMapper chatMessageMapper) {
        this.chatConversationRepository = chatConversationRepository;
        this.aiChatService = aiChatService;
        this.chatMessageMapper = chatMessageMapper;
    }



    @Transactional
    public List<ChatMessageRetrieveResponse> createConversation() {
        ChatMessage systemMessage = new ChatMessage(SenderType.SYSTEM,"I'm your personal AI assistant, please ask me anything about the system and I'll try to help you");
        ChatConversation conversation = new ChatConversation();

        conversation.addMessage(systemMessage);

        ChatConversation savedConversation = chatConversationRepository.save(conversation);

        return chatMessageMapper.toChatMessageResponseListDto(savedConversation.getMessages());
    }



    @Transactional(readOnly = true)
    public List<ChatMessageRetrieveResponse> getConversation(Long conversationId) {
        ChatConversation conversation = chatConversationRepository.findById(conversationId)
                .orElseThrow(() -> new NoSuchElementException("conversation not found"));

        return chatMessageMapper.toChatMessageResponseListDto(conversation.getMessages());
    }



    @Transactional
    public ChatMessageResponse sendMessageAndGenerateResponse(Long conversationId, String userMessage) {
        ChatConversation conversation = chatConversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        List<ChatMessage> history = conversation.getMessages().stream()
                .sorted(Comparator.comparing(ChatMessage::getId))
                .skip(Math.max(0, conversation.getMessages().size() - 20))
                .toList();

        String aiResponse = aiChatService.generateResponse(history, userMessage);

        ChatMessage userChatMessage = new ChatMessage(SenderType.USER, userMessage);
        conversation.addMessage(userChatMessage);

        ChatMessage assistantChatMessage = new ChatMessage(SenderType.ASSISTANT, aiResponse);
        conversation.addMessage(assistantChatMessage);

        chatConversationRepository.save(conversation);

        return new ChatMessageResponse(
                assistantChatMessage.getId(),
                conversation.getId(),
                userChatMessage.getContent(),
                assistantChatMessage.getContent()
        );
    }

}






