package com.example.project.dto.response;

import com.example.project.domain.enums.Role;

public record UserResponse(
        Long id,
        String name,
        String email,
        String phone,
        String address,
        Role role) {
}