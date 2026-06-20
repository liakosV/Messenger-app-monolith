package com.project.messenger.dto.conversation;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationInsertDto {

    @NotBlank(message = "Participant uuid is required")
    private String participantUuid;
}
