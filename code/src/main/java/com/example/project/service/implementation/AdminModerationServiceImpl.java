package com.example.project.service.implementation;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.project.model.Comment;
import com.example.project.repository.CommentRepository;
import com.example.project.service.AdminModerationService;
import com.example.project.service.ArtworkService;

@Service
public class AdminModerationServiceImpl implements AdminModerationService {

    private final ArtworkService artworkService;
    private final CommentRepository commentRepository;

    public AdminModerationServiceImpl(ArtworkService artworkService,
            CommentRepository commentRepository) {
        this.artworkService = artworkService;
        this.commentRepository = commentRepository;
    }

    @Override
    @Transactional
    public void deleteArtwork(Long artworkId) {
        artworkService.deleteArtworkAsAdmin(artworkId);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        commentRepository.delete(findComment(commentId));
    }

    @Override
    @Transactional
    public void deleteComment(Long biddingId, Long commentId) {
        Comment comment = findComment(commentId);
        if (comment.getBidding() == null || !biddingId.equals(comment.getBidding().getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found: " + commentId);
        }
        commentRepository.delete(comment);
    }

    private Comment findComment(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Comment not found: " + commentId));
    }

}