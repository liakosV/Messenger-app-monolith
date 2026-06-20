package com.project.messenger.dto.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageUpdateDto {

    @NotBlank(message = "Message content is required")
    @Size(max = 2000, message = "Message cannot be more than 2000 characters")
    private String content;
}
