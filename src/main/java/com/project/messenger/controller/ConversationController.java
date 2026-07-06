package com.project.messenger.controller;

import com.project.messenger.dto.conversation.ConversationInsertDto;
import com.project.messenger.dto.conversation.ConversationReadDto;
import com.project.messenger.dto.conversation.ConversationSummaryDto;
import com.project.messenger.service.ConversationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping("/conversations")
    public ResponseEntity<ConversationReadDto> createConversation(@Valid @RequestBody ConversationInsertDto insertDto) {
        ConversationReadDto readDto = conversationService.createConversation(insertDto);

        return ResponseEntity.ok(readDto);
    }

    @DeleteMapping("/conversations/{conversationUuid}")
    public ResponseEntity<Void> deleteConversation(@PathVariable UUID conversationUuid) {
        conversationService.deleteConversation(conversationUuid);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/{userUuid}/conversations")
    public ResponseEntity<List<ConversationSummaryDto>> getAllConversationForUser(@PathVariable UUID userUuid) {
        return ResponseEntity.ok(conversationService.getAllConversationsForUser(userUuid));
    }

    @GetMapping("/conversations/{conversationUuid}")
    public ResponseEntity<ConversationReadDto> getConversation(@PathVariable UUID conversationUuid) {
        return ResponseEntity.ok(conversationService.getConversation(conversationUuid));
    }
}
