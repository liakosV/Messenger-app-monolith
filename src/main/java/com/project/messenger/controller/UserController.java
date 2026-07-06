package com.project.messenger.controller;

import com.project.messenger.dto.user.UserReadDto;
import com.project.messenger.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    @DeleteMapping("/{userUuid}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userUuid) {
        userService.deleteUserByUuid(userUuid);
        return ResponseEntity.noContent().build();
    }
}
