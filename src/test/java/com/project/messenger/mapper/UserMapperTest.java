package com.project.messenger.mapper;

import com.project.messenger.dto.user.UserReadDto;
import com.project.messenger.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class UserMapperTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserMapper userMapper;

    @Test
    void mapDeletedUserToDisplayDeletedUserAsUsername() {
        User user = new User();
        user.setUsername("John");
        user.setDeleted(true);

        UserReadDto result = userMapper.mapToUserReadDto(user);

        assertEquals("Deleted User", result.getUsername());
    }
}
