package com.project.messenger.controller;

import com.project.messenger.dto.conversation.ConversationInsertDto;
import com.project.messenger.dto.conversation.ConversationReadDto;
import com.project.messenger.dto.conversation.ConversationSummaryDto;
import com.project.messenger.model.User;
import com.project.messenger.service.ConversationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping("/conversations")
    public ResponseEntity<ConversationReadDto> createConversation(@Valid @RequestBody ConversationInsertDto insertDto, @AuthenticationPrincipal User loggedInUser) {
        ConversationReadDto readDto = conversationService.createConversation(insertDto, loggedInUser.getUuid());

        return ResponseEntity.ok(readDto);
    }

    @DeleteMapping("/conversations/{conversationUuid}")
    public ResponseEntity<Void> deleteConversation(@PathVariable UUID conversationUuid, @AuthenticationPrincipal User loggedInUser) {
        conversationService.deleteConversation(conversationUuid, loggedInUser.getUuid());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationSummaryDto>> getAllConversationForUser(@AuthenticationPrincipal User loggedInUser) {
        return ResponseEntity.ok(conversationService.getAllConversationsForUser(loggedInUser.getUuid()));
    }

    @GetMapping("/conversations/{conversationUuid}")
    public ResponseEntity<ConversationReadDto> getConversation(@PathVariable UUID conversationUuid, @AuthenticationPrincipal User loggedInUser) {
        return ResponseEntity.ok(conversationService.getConversation(conversationUuid, loggedInUser.getUuid()));
    }
}
