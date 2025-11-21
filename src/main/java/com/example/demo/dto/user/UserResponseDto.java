package com.example.demo.dto.user;

import com.example.demo.model.user.UserEntity;

public record UserResponseDto(
        String id,
        String username,
        UserEntity.UserRole role
){}
