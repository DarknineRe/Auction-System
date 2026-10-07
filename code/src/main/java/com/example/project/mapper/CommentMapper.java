package com.example.project.mapper;

import org.springframework.stereotype.Component;

import com.example.project.dto.response.CommentResponse;
import com.example.project.model.Comment;
@Component
public class CommentMapper {

    public CommentResponse toResponse(Comment comment) {
        Long biddingId = comment.getBidding() == null
                ? null
                : comment.getBidding().getId();

        Long userId = comment.getUser() == null
                ? null
                : comment.getUser().getId();

        return new CommentResponse(
                comment.getId(),
                biddingId,
                userId,
                comment.getMessage(),
                comment.getThumbsup(),
                comment.getThumbsdown());
    }
}
