package com.example.project.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.project.model.User;

public interface AdminUserService {
    boolean createAdminIfAbsent(String name, String email, String rawPassword);

    Page<User> getUsers(User.Role role, Pageable pageable);

    User getUserById(Long userId);

    User setEnabled(String actorEmail, Long userId, boolean enabled);
}