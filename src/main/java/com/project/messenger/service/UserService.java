package com.project.messenger.service;

import com.project.messenger.core.exception.AppObjectAlreadyExistsException;
import com.project.messenger.core.exception.AppObjectNotFoundException;
import com.project.messenger.dto.user.UserInsertDto;
import com.project.messenger.dto.user.UserReadDto;
import com.project.messenger.mapper.UserMapper;
import com.project.messenger.model.User;
import com.project.messenger.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

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
     * Deletes a user by UUID.
     *
     * @param uuid the public UUID of the user to delete
     * @throws AppObjectNotFoundException if no user exists with the given UUID
     */
    @Transactional
    public void deleteUserByUuid(UUID uuid) {
        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> new AppObjectNotFoundException("User", "User not found"));

        userRepository.delete(user);
    }


}
