package com.example.project.dto.response;

public record ArtworkResponse(
        Long id,
        String title,
        String imageUrl,
        Long sellerprofileId,
        Long sellerUserId) {
}