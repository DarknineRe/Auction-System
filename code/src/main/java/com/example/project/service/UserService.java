package com.example.project.service;

import com.example.project.model.User;

public interface UserService {
    User registerUser(String name, String email, String password, String phone, String address);

    User getCurrentUser(String email);

    User updateCurrentUser(String email, String name, String phone, String address);

    void changePassword(String email, String currentPassword, String newPassword);
}