package com.project.messenger.controller;

import com.project.messenger.dto.authentication.AuthResponseDto;
import com.project.messenger.dto.authentication.LoginRequestDto;
import com.project.messenger.dto.authentication.RefreshTokenRequestDto;
import com.project.messenger.dto.user.UserInsertDto;
import com.project.messenger.dto.user.UserReadDto;
import com.project.messenger.service.AuthService;
import com.project.messenger.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserReadDto> registerUser(@Valid @RequestBody UserInsertDto insertDto) {
        UserReadDto readDto = userService.createUser(insertDto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/users/{uuid}")
                .buildAndExpand(readDto.getUuid())
                .toUri();

        return ResponseEntity.created(location)
                .body(readDto);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody @Valid LoginRequestDto requestDto) {
        AuthResponseDto authResponseDto = authService.authenticate(requestDto);

        return ResponseEntity.ok(authResponseDto);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDto> refresh(@Valid @RequestBody RefreshTokenRequestDto requestDto) {
        AuthResponseDto authResponseDto = authService.refresh(requestDto.refreshToken());

        return ResponseEntity.ok(authResponseDto);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequestDto requestDto) {
        authService.logout(requestDto.refreshToken());

        return ResponseEntity.noContent().build();
    }
}
