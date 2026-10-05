package com.example.project.service;

public interface AdminUserService {
    boolean createAdminIfAbsent(String name, String email, String rawPassword);
}