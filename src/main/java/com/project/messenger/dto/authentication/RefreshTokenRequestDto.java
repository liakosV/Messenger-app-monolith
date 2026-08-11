package com.project.messenger.dto.authentication;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequestDto(

        @NotBlank(message = "Refresh Token is required")
        String refreshToken
) {
}
