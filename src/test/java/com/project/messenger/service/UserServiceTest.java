package com.project.messenger.service;

import com.project.messenger.core.exception.AppObjectAlreadyExistsException;
import com.project.messenger.core.exception.AppObjectNotFoundException;
import com.project.messenger.core.exception.AppObjectUnauthorizedException;
import com.project.messenger.dto.user.UserInsertDto;
import com.project.messenger.dto.user.UserReadDto;
import com.project.messenger.mapper.UserMapper;
import com.project.messenger.model.User;
import com.project.messenger.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void createUserSuccessfully() {
        User user = new User();

        UserInsertDto insertDto = new UserInsertDto(
                "john",
                "john@example.com",
                "Password1!",
                LocalDate.of(2000, 1, 1),
                "+306900000000"
        );

        UserReadDto expectedDto = mock(UserReadDto.class);

        when(userRepository.existsByUsername(insertDto.getUsername()))
                .thenReturn(false);

        when(userRepository.existsByEmail(insertDto.getEmail()))
                .thenReturn(false);

        when(userRepository.existsByPhoneNumber(insertDto.getPhoneNumber()))
                .thenReturn(false);

        when(userMapper.mapToUserEntity(insertDto))
                .thenReturn(user);

        when(userRepository.save(user))
                .thenReturn(user);

        when(userMapper.mapToUserReadDto(user))
                .thenReturn(expectedDto);

        UserReadDto actualDto = userService.createUser(insertDto);

        assertSame(expectedDto, actualDto);
        verify(userRepository).existsByUsername(insertDto.getUsername());
        verify(userRepository).existsByEmail(insertDto.getEmail());
        verify(userRepository).existsByPhoneNumber(insertDto.getPhoneNumber());
        verify(userMapper).mapToUserEntity(insertDto);
        verify(userRepository).save(user);
        verify(userMapper).mapToUserReadDto(user);
    }

    @Test
    void createUserWhenUsernameAlreadyExists() {
        UserInsertDto insertDto = new UserInsertDto(
                "john",
                "john@example.com",
                "Password1!",
                LocalDate.of(2000, 1, 1),
                "+306900000000"
        );

        when(userRepository.existsByUsername(insertDto.getUsername()))
                .thenReturn(true);

        AppObjectAlreadyExistsException exception = assertThrows(
                AppObjectAlreadyExistsException.class,
                () -> userService.createUser(insertDto)
        );

        assertEquals("Username already exists", exception.getMessage());
        verifyNoInteractions(userMapper);
        verify(userRepository).existsByUsername(insertDto.getUsername());
        verify(userRepository, never()).existsByEmail(insertDto.getEmail());
        verify(userRepository, never()).existsByPhoneNumber(insertDto.getPhoneNumber());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUserWhenEmailAlreadyExists() {
        UserInsertDto insertDto = new UserInsertDto(
                "john",
                "john@example.com",
                "Password1!",
                LocalDate.of(2000, 1, 1),
                "+306900000000"
        );

        when(userRepository.existsByUsername(insertDto.getUsername()))
                .thenReturn(false);

        when(userRepository.existsByEmail(insertDto.getEmail()))
                .thenReturn(true);

        AppObjectAlreadyExistsException exception = assertThrows(
                AppObjectAlreadyExistsException.class,
                () -> userService.createUser(insertDto)
        );

        assertEquals("Email already exists", exception.getMessage());
        verifyNoInteractions(userMapper);
        verify(userRepository).existsByUsername(insertDto.getUsername());
        verify(userRepository).existsByEmail(insertDto.getEmail());
        verify(userRepository, never()).existsByPhoneNumber(insertDto.getPhoneNumber());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUserWhenPhoneNumberAlreadyExists() {
        UserInsertDto insertDto = new UserInsertDto(
                "john",
                "john@example.com",
                "Password1!",
                LocalDate.of(2000, 1, 1),
                "+306900000000"
        );

        when(userRepository.existsByUsername(insertDto.getUsername()))
                .thenReturn(false);

        when(userRepository.existsByEmail(insertDto.getEmail()))
                .thenReturn(false);

        when(userRepository.existsByPhoneNumber(insertDto.getPhoneNumber()))
                .thenReturn(true);

        AppObjectAlreadyExistsException exception = assertThrows(
                AppObjectAlreadyExistsException.class,
                () -> userService.createUser(insertDto)
        );

        assertEquals("Phone number already exists", exception.getMessage());
        verifyNoInteractions(userMapper);
        verify(userRepository).existsByUsername(insertDto.getUsername());
        verify(userRepository).existsByEmail(insertDto.getEmail());
        verify(userRepository).existsByPhoneNumber(insertDto.getPhoneNumber());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserByUuidSuccessfully() {
        UUID userUuid = UUID.randomUUID();

        User user = new User();

        UserReadDto expectedDto = mock(UserReadDto.class);

        when(userRepository.findByUuid(userUuid))
                .thenReturn(Optional.of(user));

        when(userMapper.mapToUserReadDto(user))
                .thenReturn(expectedDto);

        UserReadDto actualDto = userService.getUserByUuid(userUuid);

        assertSame(expectedDto, actualDto);

        verify(userRepository).findByUuid(userUuid);
        verify(userMapper).mapToUserReadDto(user);
    }

    @Test
    void getUserByUuidWhenUserNotFound() {
        UUID userUuid = UUID.randomUUID();

        when(userRepository.findByUuid(userUuid))
                .thenReturn(Optional.empty());

        AppObjectNotFoundException exception = assertThrows(
                AppObjectNotFoundException.class,
                () -> userService.getUserByUuid(userUuid)
        );

        assertEquals("User not found", exception.getMessage());

        verify(userRepository).findByUuid(userUuid);
        verifyNoInteractions(userMapper);
    }

    @Test
    void getAllUsersSuccessfully() {
        User firstUser = new User();
        User secondUser = new User();

        UserReadDto firstDto = mock(UserReadDto.class);
        UserReadDto secondDto = mock(UserReadDto.class);

        when(userRepository.findAll())
                .thenReturn(List.of(firstUser, secondUser));

        when(userMapper.mapToUserReadDto(firstUser))
                .thenReturn(firstDto);

        when(userMapper.mapToUserReadDto(secondUser))
                .thenReturn(secondDto);

        List<UserReadDto> actualDtos = userService.getAllUsers();

        assertEquals(List.of(firstDto, secondDto), actualDtos);

        verify(userRepository).findAll();
        verify(userMapper).mapToUserReadDto(firstUser);
        verify(userMapper).mapToUserReadDto(secondUser);
    }

    @Test
    void getAllUsersWhenNoUsersExistReturnEmptyList() {
        when(userRepository.findAll())
                .thenReturn(List.of());

        List<UserReadDto> result = userService.getAllUsers();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository).findAll();
        verifyNoInteractions(userMapper);
    }

    @Test
    void deleteCurrentUserSuccessfully() {
        UUID userUuid = UUID.randomUUID();
        String rawPassword = "!Password1";

        User user = new User();
        user.setPassword(rawPassword);

        when(userRepository.findByUuid(userUuid))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(rawPassword, user.getPassword()))
                .thenReturn(true);

        userService.deleteCurrentUser(userUuid, rawPassword);

        assertTrue(user.isDeleted());
        assertNotNull(user.getDeletedAt());

        verify(userRepository, never()).delete(any(User.class));
        verify(userRepository).findByUuid(userUuid);
        verify(passwordEncoder).matches(rawPassword, user.getPassword());

    }

    @Test
    void deleteCurrentUserWhenUserNotFound() {
        UUID userUuid = UUID.randomUUID();
        String rawPassword = "!Password1";

        when(userRepository.findByUuid(userUuid))
                .thenReturn(Optional.empty());

        AppObjectNotFoundException exception = assertThrows(
                AppObjectNotFoundException.class,
                () -> userService.deleteCurrentUser(userUuid, rawPassword)
        );

        assertEquals("User not found", exception.getMessage());
        verifyNoInteractions(passwordEncoder);
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void deleteCurrentUserWhenPasswordDoesNotMatch() {
        UUID userUuid = UUID.randomUUID();
        String userPassword = "!Password1";
        String rawPassword = "WrongPassword";

        User user = new User();
        user.setPassword(userPassword);

        when(userRepository.findByUuid(userUuid))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(rawPassword, user.getPassword()))
                .thenReturn(false);

        AppObjectUnauthorizedException exception = assertThrows(
                AppObjectUnauthorizedException.class,
                () -> userService.deleteCurrentUser(userUuid, rawPassword)
        );

        assertEquals("The password is incorrect", exception.getMessage());

        verify(userRepository).findByUuid(userUuid);
        verify(passwordEncoder).matches(rawPassword, user.getPassword());
        verify(userRepository, never()).delete(user);

    }
}
