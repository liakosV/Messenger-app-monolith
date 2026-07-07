package com.project.messenger.controller;

import com.project.messenger.dto.message.MessageInsertDto;
import com.project.messenger.dto.message.MessageReadDto;
import com.project.messenger.dto.message.MessageUpdateDto;
import com.project.messenger.model.User;
import com.project.messenger.service.MessageService;
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
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/conversations/{conversationUuid}/messages")
    public ResponseEntity<MessageReadDto> sendMessage(
            @PathVariable UUID conversationUuid,
            @AuthenticationPrincipal User loggedInUser,
            @RequestBody @Valid MessageInsertDto insertDto
            ) {
        MessageReadDto readDto = messageService.sendMessage(conversationUuid, loggedInUser.getUuid(), insertDto);

        return ResponseEntity.ok(readDto);
    }

    @PatchMapping("/messages/{messageUuid}")
    public ResponseEntity<MessageReadDto> editMessage(
            @Valid @RequestBody MessageUpdateDto updateDto,
            @PathVariable UUID messageUuid,
            @AuthenticationPrincipal User loggedInUser
            ) {
        MessageReadDto readDto = messageService.editMessage(updateDto, messageUuid, loggedInUser.getUuid());

        return ResponseEntity.ok(readDto);
    }

    @DeleteMapping("/messages/{messageUuid}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable UUID messageUuid,
            @AuthenticationPrincipal User loggedInUser
    ) {
        messageService.deleteMessage(messageUuid, loggedInUser.getUuid());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/conversations/{conversationUuid}/messages")
    public ResponseEntity<List<MessageReadDto>> getAllMessagesByConversationUuid(
            @PathVariable UUID conversationUuid
    ) {
        return ResponseEntity.ok(messageService.getAllMessagesByConversationUuid(conversationUuid));
    }

    @GetMapping("/messages/{messageUuid}")
    public ResponseEntity<MessageReadDto> getMessage(@PathVariable UUID messageUuid) {
        return ResponseEntity.ok(messageService.getMessage(messageUuid));
    }
}
