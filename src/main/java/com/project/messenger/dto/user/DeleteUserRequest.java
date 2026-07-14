package com.project.messenger.dto.user;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body used to confirm an account deletion with the user's password.
 *
 * @param password the user's unencoded password
 */
public record DeleteUserRequest(
        @NotBlank String password
) {
}
