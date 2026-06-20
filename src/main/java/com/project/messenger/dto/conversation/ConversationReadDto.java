package com.project.messenger.dto.conversation;

import com.project.messenger.dto.message.MessageReadDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationReadDto {

    private Long id;
    private UUID uuid;

    private Set<ConversationParticipantDto> participants;

    private List<MessageReadDto> messages;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
