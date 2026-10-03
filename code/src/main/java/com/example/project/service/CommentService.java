package com.example.project.service;

import java.util.List;

import com.example.project.model.Comment;

public interface CommentService {
    Comment createComment(Long biddingID, Long userID, String message);

    List<Comment> getCommentsByBiddingId(Long biddingID);

    Comment updateComment(Long biddingID, Long commentID, Long userID, String message);

    void deleteComment(Long biddingID, Long commentID, Long userID);

    Comment likeComment(Long biddingID, Long commentID);

    Comment dislikeComment(Long biddingID, Long commentID);
}
