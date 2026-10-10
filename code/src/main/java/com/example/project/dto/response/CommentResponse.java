package com.example.project.dto.response;

public record CommentResponse(
        Long id,
        Long biddingId,
        Long userId,
        String message,
        int thumbsup,
        int thumbsdown) {
}
