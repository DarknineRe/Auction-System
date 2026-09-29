package com.example.project.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateArtworkRequest(
        @NotBlank(message = "title is required")
        String title,

        String imageUrl) {
}