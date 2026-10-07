package com.example.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateArtworkRequest(
        @NotNull @Positive Long sellerUserId,
        @NotBlank @Size(max = 255) String title,
        @Size(max = 2048) String imageUrl) {
}