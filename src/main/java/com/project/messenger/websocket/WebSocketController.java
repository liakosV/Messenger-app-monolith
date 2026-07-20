package com.project.messenger.websocket;

import com.project.messenger.dto.message.MessageInsertDto;
import com.project.messenger.dto.message.MessageReadDto;
import com.project.messenger.model.User;
import com.project.messenger.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/conversations/{conversationUuid}/messages")
    public void sendMessage(
            @DestinationVariable UUID conversationUuid,
            @Valid MessageInsertDto insertDto,
            Authentication authentication
            ) {
        User sender = (User) authentication.getPrincipal();

        MessageReadDto savedMessage = messageService.sendMessage(conversationUuid, sender.getUuid(), insertDto);

        messagingTemplate.convertAndSend("/topic/conversations/" + conversationUuid, savedMessage);
    }
}
