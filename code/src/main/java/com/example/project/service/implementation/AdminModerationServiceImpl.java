package com.example.project.service.implementation;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.project.model.Bidding;
import com.example.project.repository.BiddingRepository;
import com.example.project.repository.CommentRepository;
import com.example.project.service.AdminModerationService;
import com.example.project.service.ArtworkService;

@Service
public class AdminModerationServiceImpl implements AdminModerationService {

    private final ArtworkService artworkService;
    private final BiddingRepository biddingRepository;
    private final CommentRepository commentRepository;

    public AdminModerationServiceImpl(ArtworkService artworkService,
            BiddingRepository biddingRepository,
            CommentRepository commentRepository) {
        this.artworkService = artworkService;
        this.biddingRepository = biddingRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    @Transactional
    public void deleteArtwork(Long artworkId) {
        if (biddingRepository.existsByArtworks_IdAndStatus(artworkId, Bidding.Status.ACTIVE)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Artwork is part of an active bidding. Cancel the bidding first.");
        }
        artworkService.deleteArtwork(artworkId);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found: " + commentId);
        }
        commentRepository.deleteById(commentId);
    }
}