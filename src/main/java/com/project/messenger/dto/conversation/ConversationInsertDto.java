package com.project.messenger.dto.conversation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationInsertDto {

    @NotEmpty(message = "At least one participant is required")
    private Set<UUID> participantUuids;
}
