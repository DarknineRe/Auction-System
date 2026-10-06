package com.example.project.dto.response;

import com.example.project.model.User;

public record AdminUserResponse(
        Long id,
        String name,
        String email,
        String phone,
        String address,
        User.Role role,
        boolean enabled) {
}