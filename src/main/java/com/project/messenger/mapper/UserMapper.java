package com.project.messenger.mapper;

import com.project.messenger.dto.user.UserInsertDto;
import com.project.messenger.dto.user.UserReadDto;
import com.project.messenger.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final PasswordEncoder passwordEncoder;

    public UserReadDto mapToUserReadDto(User user) {
        return new UserReadDto(
                user.getId(),
                user.getUuid(),
                user.getUsername(),
                user.getEmail(),
                user.getDateOfBirth(),
                user.getPhoneNumber()
        );
    }

    public User mapToUserEntity(UserInsertDto dto) {
        User user = new User();

        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setDateOfBirth(dto.getDateOfBirth());
        user.setPhoneNumber(dto.getPhoneNumber());

        return user;
    }
}
