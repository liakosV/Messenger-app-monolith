package com.project.messenger.service;

import com.project.messenger.dto.authentication.AuthResponseDto;
import com.project.messenger.dto.authentication.LoginRequestDto;
import com.project.messenger.model.User;
import com.project.messenger.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    public AuthResponseDto authenticate(LoginRequestDto requestDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(requestDto.getUsername(), requestDto.getPassword())
        );

        User user = (User) authentication.getPrincipal();
        String accessToken = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.issue(user);

        return new AuthResponseDto(accessToken, refreshToken);
    }

    @Transactional
    public AuthResponseDto refresh(String refreshToken) {
        User user = refreshTokenService.consume(refreshToken);

        String newAccessToken = jwtService.generateToken(user);
        String newRefreshToken = refreshTokenService.issue(user);

        return new AuthResponseDto(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.consume(refreshToken);
    }
}
