package com.project.messenger.dto.conversation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversationParticipantDto {

    private Long id;
    private UUID uuid;
    private String username;
}
