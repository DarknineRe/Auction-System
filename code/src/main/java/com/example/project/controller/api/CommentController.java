package com.example.project.controller.api;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.project.dto.request.CreateCommentRequest;
import com.example.project.dto.response.CommentResponse;
import com.example.project.mapper.CommentMapper;
import com.example.project.model.Comment;
import com.example.project.model.User;
import com.example.project.service.CommentService;
import com.example.project.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/biddings/{biddingId}/comments")
public class CommentController {

    private final CommentService commentService;
    private final UserService userService;
    private final CommentMapper commentMapper;

    public CommentController(
            CommentService commentService,
            UserService userService,
            CommentMapper commentMapper) {
        this.commentService = commentService;
        this.userService = userService;
        this.commentMapper = commentMapper;
    }

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long biddingId,
            Authentication authentication,
            @Valid @RequestBody CreateCommentRequest request) {
        User user = userService.getCurrentUser(authentication.getName());
        Comment comment = commentService.createComment(
                biddingId, user.getId(), request.message());

        return ResponseEntity.status(HttpStatus.CREATED).body(commentMapper.toResponse(comment));
    }

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long biddingId) {
        List<CommentResponse> responses = commentService.getCommentsByBiddingId(biddingId)
                .stream()
                .map(commentMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long biddingId,
            @PathVariable Long commentId,
            Authentication authentication,
            @Valid @RequestBody CreateCommentRequest request) {
        User user = userService.getCurrentUser(authentication.getName());
        Comment comment = commentService.updateComment(
                biddingId, commentId, user.getId(), request.message());

        return ResponseEntity.ok(commentMapper.toResponse(comment));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long biddingId,
            @PathVariable Long commentId,
            Authentication authentication) {
        User user = userService.getCurrentUser(authentication.getName());
        commentService.deleteComment(biddingId, commentId, user.getId());

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{commentId}/like")
    public ResponseEntity<CommentResponse> likeComment(
            @PathVariable Long biddingId,
            @PathVariable Long commentId) {
        return ResponseEntity.ok(commentMapper.toResponse(
                commentService.likeComment(biddingId, commentId)));
    }

    @PostMapping("/{commentId}/dislike")
    public ResponseEntity<CommentResponse> dislikeComment(
            @PathVariable Long biddingId,
            @PathVariable Long commentId) {
        return ResponseEntity.ok(commentMapper.toResponse(
                commentService.dislikeComment(biddingId, commentId)));
    }
}
