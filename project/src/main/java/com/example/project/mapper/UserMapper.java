package com.example.project.mapper;

import org.springframework.stereotype.Component;

import com.example.project.domain.entity.User;
import com.example.project.dto.response.UserResponse;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getAddress(),
                user.getRole());
    }
}