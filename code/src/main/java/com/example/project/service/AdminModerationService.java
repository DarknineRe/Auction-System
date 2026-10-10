package com.example.project.service;

public interface AdminModerationService {
    void deleteArtwork(Long artworkId);

    void deleteComment(Long commentId);

    void deleteComment(Long biddingId, Long commentId);
}