package com.project.messenger.service;

import com.project.messenger.core.exception.AppObjectAlreadyExistsException;
import com.project.messenger.core.exception.AppObjectNotFoundException;
import com.project.messenger.core.exception.AppObjectUnauthorizedException;
import com.project.messenger.dto.user.UserInsertDto;
import com.project.messenger.dto.user.UserReadDto;
import com.project.messenger.mapper.UserMapper;
import com.project.messenger.model.User;
import com.project.messenger.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Creates a new user after checking that the username, email, and phone number are unique.
     *
     * @param insertDto the data needed to create the user
     * @return the created user as a read DTO
     * @throws AppObjectAlreadyExistsException if username, email, or phone number is already used
     */
    @Transactional
    public UserReadDto createUser(UserInsertDto insertDto) {
        if (userRepository.existsByUsername(insertDto.getUsername())) {
            throw new AppObjectAlreadyExistsException("User", "Username already exists");
        }

        if (userRepository.existsByEmail(insertDto.getEmail())) {
            throw new AppObjectAlreadyExistsException("User", "Email already exists");
        }

        if (userRepository.existsByPhoneNumber(insertDto.getPhoneNumber())) {
            throw new AppObjectAlreadyExistsException("User", "Phone number already exists");
        }

        User user = userMapper.mapToUserEntity(insertDto);
        User savedUser = userRepository.save(user);

        return userMapper.mapToUserReadDto(savedUser);
    }

    /**
     * Finds a user by UUID.
     *
     * @param uuid the public UUID of the user
     * @return the matching user as a read DTO
     * @throws AppObjectNotFoundException if no user exists with the given UUID
     */
    @Transactional(readOnly = true)
    public UserReadDto getUserByUuid(UUID uuid) {
        return userRepository.findByUuid(uuid)
                .map(userMapper::mapToUserReadDto)
                .orElseThrow(() -> new AppObjectNotFoundException("User", "User not found"));
    }

    /**
     * Finds all users.
     *
     * @return all users as read DTOs
     */
    @Transactional(readOnly = true)
    public List<UserReadDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::mapToUserReadDto)
                .toList();
    }

    /**
     * Deletes the currently authenticated user's account after verifying their password.
     * The authenticated principal must have the same UUID as {@code userUuid}.
     *
     * @param userUuid    the public UUID of the authenticated user
     * @param rawPassword the unencoded password supplied for account confirmation
     * @throws AppObjectNotFoundException     if no user exists with the supplied UUID
     * @throws AppObjectUnauthorizedException if the supplied password is incorrect
     */
    @PreAuthorize("principal.uuid == #userUuid")
    @Transactional
    public void deleteCurrentUser(UUID userUuid, String rawPassword) {
        User user = userRepository.findByUuid(userUuid)
                .orElseThrow(() -> new AppObjectNotFoundException("User", "User not found"));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new AppObjectUnauthorizedException("User", "The password is incorrect");
        }

        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
    }


}
