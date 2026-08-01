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

    @InjectMocks
    private AuthService authService;

    @Test
    void authenticateUserSuccessfully() {
        LoginRequestDto requestDto = new LoginRequestDto("john", "Password1!");
        String accessToken = "generated.jwt.token";

        User user = new User();
        user.setUsername("john");

        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(authentication);

        when(authentication.getPrincipal())
                .thenReturn(user);

        when(jwtService.generateToken(user))
                .thenReturn(accessToken);

        AuthResponseDto responseDto = authService.authenticate(requestDto);

        assertNotNull(responseDto);
        assertEquals(accessToken, responseDto.getToken());

        ArgumentCaptor<Authentication> argumentCaptor = ArgumentCaptor.forClass(Authentication.class);

        verify(authenticationManager).authenticate(argumentCaptor.capture());

        Authentication submittedAuthentication = argumentCaptor.getValue();

        assertEquals("john", submittedAuthentication.getPrincipal());
        assertEquals("Password1!", submittedAuthentication.getCredentials());

        verify(jwtService).generateToken(user);
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
        verifyNoInteractions(jwtService);
    }

}
