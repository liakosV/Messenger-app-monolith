package com.project.messenger.controller;

import com.project.messenger.dto.user.DeleteUserRequest;
import com.project.messenger.dto.user.UserReadDto;
import com.project.messenger.model.User;
import com.project.messenger.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/{userUuid}")
    public ResponseEntity<UserReadDto> getUserByUuid(@PathVariable UUID userUuid) {
        return ResponseEntity.ok(userService.getUserByUuid(userUuid));
    }

    @GetMapping
    public ResponseEntity<List<UserReadDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal User loggedInUser, @Valid @RequestBody DeleteUserRequest request) {
        userService.deleteCurrentUser(loggedInUser.getUuid(), request.password());
        return ResponseEntity.noContent().build();
    }
}
