package com.project.messenger.service;

import com.project.messenger.core.exception.AppObjectAlreadyExistsException;
import com.project.messenger.dto.user.UserInsertDto;
import com.project.messenger.dto.user.UserReadDto;
import com.project.messenger.mapper.UserMapper;
import com.project.messenger.model.User;
import com.project.messenger.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

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

    //todo get,get all, delete


}
