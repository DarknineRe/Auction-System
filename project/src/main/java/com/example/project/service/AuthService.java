package com.example.project.service;

import com.example.project.dto.response.UserResponse;

public interface AuthService {
    UserResponse register(String name, String email, String rawPassword, String phone, String address);

    UserResponse login(String email, String rawPassword);
}