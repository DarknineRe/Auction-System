package com.example.project.dto.request;

import com.example.project.model.User;

import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(@NotNull User.Role role) {
}
