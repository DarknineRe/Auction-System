package com.example.project.controller.web;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.project.model.User;
import com.example.project.service.AdminModerationService;
import com.example.project.service.CommentService;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Posting, reacting to, editing and deleting comments on a bidding. */
@Controller
@Validated
public class CommentPageController {

    private final CommentService commentService;
    private final AdminModerationService adminModerationService;
    private final WebSupport web;

    public CommentPageController(CommentService commentService, AdminModerationService adminModerationService,
            WebSupport web) {
        this.commentService = commentService;
        this.adminModerationService = adminModerationService;
        this.web = web;
    }

    @PostMapping("/biddings/{biddingId}/comments")
    public String createComment(
            @PathVariable Long biddingId,
            Authentication authentication,
            @RequestParam @NotBlank @Size(max = 2000) String message) {
        User user = web.currentUser(authentication);
        commentService.createComment(biddingId, user.getId(), message.trim());
        return "redirect:/biddings/" + biddingId + "?commentPosted";
    }

    @PostMapping("/biddings/{biddingId}/comments/{commentId}/like")
    public String likeComment(
            @PathVariable Long biddingId, @PathVariable Long commentId, Authentication authentication) {
        commentService.likeComment(biddingId, commentId, web.currentUser(authentication).getId());
        return commentsAnchor(biddingId);
    }

    @PostMapping("/biddings/{biddingId}/comments/{commentId}/dislike")
    public String dislikeComment(
            @PathVariable Long biddingId, @PathVariable Long commentId, Authentication authentication) {
        commentService.dislikeComment(biddingId, commentId, web.currentUser(authentication).getId());
        return commentsAnchor(biddingId);
    }

    @PostMapping("/biddings/{biddingId}/comments/{commentId}")
    public String updateComment(
            @PathVariable Long biddingId,
            @PathVariable Long commentId,
            Authentication authentication,
            @RequestParam @NotBlank @Size(max = 2000) String message) {
        User user = web.currentUser(authentication);
        commentService.updateComment(biddingId, commentId, user.getId(), message.trim());
        return commentsAnchor(biddingId);
    }

    @PostMapping("/biddings/{biddingId}/comments/{commentId}/delete")
    public String deleteComment(
            @PathVariable Long biddingId, @PathVariable Long commentId, Authentication authentication) {
        if (web.hasAnyRole(authentication, "ROLE_ADMIN", "ROLE_SUPER_ADMIN")) {
            adminModerationService.deleteComment(biddingId, commentId);
        } else {
            commentService.deleteComment(biddingId, commentId, web.currentUser(authentication).getId());
        }
        return commentsAnchor(biddingId);
    }

    private static String commentsAnchor(Long biddingId) {
        return "redirect:/biddings/" + biddingId + "#comments";
    }
}
