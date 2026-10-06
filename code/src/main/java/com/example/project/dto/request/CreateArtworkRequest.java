package com.example.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateArtworkRequest(
        @NotBlank @Size(max = 255) String title,
        @Size(max = 2048) String imageUrl) {
}
