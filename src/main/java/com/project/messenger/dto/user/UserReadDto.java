package com.project.messenger.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserReadDto {

    private Long id;
    private UUID uuid;
    private String username;
    private String email;
    private LocalDate dateOfBirth;
    private String phoneNumber;
}
