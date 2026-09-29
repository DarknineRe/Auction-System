package com.example.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateArtworkRequest(
        @NotBlank(message = "title is required")
        String title,

        String imageUrl,

        @NotNull(message = "sellerProfileId is required")
        Long sellerProfileId) {
}