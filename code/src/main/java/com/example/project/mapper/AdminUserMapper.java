package com.example.project.mapper;

import org.springframework.stereotype.Component;

import com.example.project.dto.response.AdminUserResponse;
import com.example.project.model.User;

@Component
public class AdminUserMapper {

    public AdminUserResponse toResponse(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getAddress(),
                user.getRole(),
                user.isEnabled());
    }
}
