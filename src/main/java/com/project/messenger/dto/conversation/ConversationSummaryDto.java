package com.project.messenger.dto.conversation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationSummaryDto {

    private Long id;
    private UUID uuid;

    private Set<ConversationParticipantDto> participants;

    private String lastMessageContent;
    private String lastMessageSenderUsername;
    private LocalDateTime lastMessageCreatedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
