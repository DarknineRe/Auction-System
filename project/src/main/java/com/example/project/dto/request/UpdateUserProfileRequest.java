package com.example.project.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserProfileRequest(
        @NotBlank String name,
        String phone,
        String address) {
}