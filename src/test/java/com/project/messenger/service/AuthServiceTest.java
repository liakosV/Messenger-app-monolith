package com.project.messenger.service;

import com.project.messenger.dto.authentication.AuthResponseDto;
import com.project.messenger.dto.authentication.LoginRequestDto;
import com.project.messenger.model.User;
import com.project.messenger.security.jwt.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    @Test
    void authenticateUserSuccessfully() {
        LoginRequestDto requestDto = new LoginRequestDto("john", "Password1!");
        String accessToken = "generated.jwt.token";
        String refreshToken = "generated.refresh.token";

        User user = new User();
        user.setUsername("john");

        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(authentication);

        when(authentication.getPrincipal())
                .thenReturn(user);

        when(jwtService.generateToken(user))
                .thenReturn(accessToken);

        when(refreshTokenService.issue(user))
                .thenReturn(refreshToken);

        AuthResponseDto responseDto = authService.authenticate(requestDto);

        assertNotNull(responseDto);
        assertEquals(accessToken, responseDto.getAccessToken());
        assertEquals(refreshToken, responseDto.getRefreshToken());

        ArgumentCaptor<Authentication> argumentCaptor = ArgumentCaptor.forClass(Authentication.class);

        verify(authenticationManager).authenticate(argumentCaptor.capture());

        Authentication submittedAuthentication = argumentCaptor.getValue();

        assertEquals("john", submittedAuthentication.getPrincipal());
        assertEquals("Password1!", submittedAuthentication.getCredentials());

        verify(jwtService).generateToken(user);
        verify(refreshTokenService).issue(user);
    }

    @Test
    void authenticateUserWhenCredentialsAreInvalid() {
        LoginRequestDto requestDto = new LoginRequestDto("john", "wrongPassword");

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("Bad Credentials"));

        assertThrows(
                BadCredentialsException.class,
                () -> authService.authenticate(requestDto)
        );

        verify(authenticationManager).authenticate(any(Authentication.class));
        verifyNoInteractions(jwtService, refreshTokenService);
    }

    @Test
    void refreshRotatesTokenSuccessfully() {
        String oldRefreshToken = "old-refresh-token";
        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";

        User user = new User();
        user.setUsername("john");

        when(refreshTokenService.consume(oldRefreshToken))
                .thenReturn(user);

        when(jwtService.generateToken(user))
                .thenReturn(newAccessToken);

        when(refreshTokenService.issue(user))
                .thenReturn(newRefreshToken);

        AuthResponseDto response =
                authService.refresh(oldRefreshToken);

        assertEquals(newAccessToken, response.getAccessToken());
        assertEquals(newRefreshToken, response.getRefreshToken());

        verify(refreshTokenService).consume(oldRefreshToken);
        verify(jwtService).generateToken(user);
        verify(refreshTokenService).issue(user);
    }

    @Test
    void logoutRevokesRefreshToken() {
        String refreshToken = "refresh-token";

        authService.logout(refreshToken);

        verify(refreshTokenService).consume(refreshToken);
        verifyNoInteractions(jwtService);
    }
}
