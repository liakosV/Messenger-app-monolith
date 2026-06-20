package com.project.messenger.dto.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageReadDto {

    private Long id;
    private UUID uuid;

    private String content;

    private Long senderId;
    private String senderUuid;
    private String senderUsername;

    private Long conversationId;
    private String conversationUuid;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
