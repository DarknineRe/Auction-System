package com.example.project.service.implementation;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.project.model.Bidding;
import com.example.project.model.Comment;
import com.example.project.model.User;
import com.example.project.repository.BiddingRepository;
import com.example.project.repository.CommentRepository;
import com.example.project.repository.UserRepository;
import com.example.project.service.CommentService;

@Service
public class CommentServiceImpl implements CommentService {

    private final BiddingRepository biddingRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public CommentServiceImpl(
            BiddingRepository biddingRepository,
            CommentRepository commentRepository,
            UserRepository userRepository) {
        this.biddingRepository = biddingRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Comment createComment(Long biddingID, Long userID, String message) {
        Bidding bidding = findBiddingById(biddingID);
        User user = userRepository.findById(userID)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Comment comment = new Comment();
        comment.setMessage(message);
        comment.setUser(user);
        bidding.addComment(comment);
        commentRepository.saveAndFlush(comment);
        biddingRepository.saveAndFlush(bidding);

        return comment;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Comment> getCommentsByBiddingId(Long biddingID) {
        findBiddingById(biddingID);
        return commentRepository.findByBidding_IdOrderByIdAsc(biddingID);
    }

    @Override
    @Transactional
    public Comment updateComment(Long biddingID, Long commentID, Long userID, String message) {
        Comment comment = findCommentInBidding(biddingID, commentID);
        ensureCommentOwner(comment, userID);
        comment.setMessage(message);
        return commentRepository.save(comment);
    }

    @Override
    @Transactional
    public void deleteComment(Long biddingID, Long commentID, Long userID) {
        Bidding bidding = findBiddingById(biddingID);
        Comment comment = findCommentInBidding(bidding, commentID);
        ensureCommentOwner(comment, userID);
        bidding.getComments().remove(comment);
        commentRepository.delete(comment);
        biddingRepository.save(bidding);
    }

    @Override
    @Transactional
    public Comment likeComment(Long biddingID, Long commentID) {
        Comment comment = findCommentInBidding(biddingID, commentID);
        comment.setThumbsup(comment.getThumbsup() + 1);
        return commentRepository.save(comment);
    }

    @Override
    @Transactional
    public Comment dislikeComment(Long biddingID, Long commentID) {
        Comment comment = findCommentInBidding(biddingID, commentID);
        comment.setThumbsdown(comment.getThumbsdown() + 1);
        return commentRepository.save(comment);
    }

    private Bidding findBiddingById(Long biddingID) {
        return biddingRepository.findById(biddingID)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bidding not found"));
    }

    private Comment findCommentInBidding(Long biddingID, Long commentID) {
        return findCommentInBidding(findBiddingById(biddingID), commentID);
    }

    private Comment findCommentInBidding(Bidding bidding, Long commentID) {
        return bidding.getComments().stream()
                .filter(comment -> commentID != null && commentID.equals(comment.getId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
    }

    private void ensureCommentOwner(Comment comment, Long userID) {
        if (comment.getUser() == null || userID == null || !comment.getUser().getId().equals(userID)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the comment author can modify it");
        }
    }
}
