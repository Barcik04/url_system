package com.example.url_system.controllers;

import com.example.url_system.dtos.chat.ChatMessageResponse;
import com.example.url_system.dtos.chat.ChatMessageRetrieveResponse;
import com.example.url_system.services.ChatConversationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/conversations")
public class ChatConversationController {
    private final ChatConversationService chatConversationService;

    public ChatConversationController(ChatConversationService chatConversationService) {
        this.chatConversationService = chatConversationService;
    }


    @PostMapping("/open")
    public ResponseEntity<List<ChatMessageRetrieveResponse>> createConversation() {
        List<ChatMessageRetrieveResponse> response = chatConversationService.createConversation();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<List<ChatMessageRetrieveResponse>> getConversation(@PathVariable Long conversationId) {
        List<ChatMessageRetrieveResponse> response = chatConversationService.getConversation(conversationId);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/{conversationId}")
    public ResponseEntity<ChatMessageResponse> sendMessageAndGenerateResponse(@PathVariable Long conversationId, @RequestBody String userMessage) {
        ChatMessageResponse chatMessageResponse = chatConversationService.sendMessageAndGenerateResponse(conversationId, userMessage);

        return ResponseEntity.status(HttpStatus.CREATED).body(chatMessageResponse);
    }
}
