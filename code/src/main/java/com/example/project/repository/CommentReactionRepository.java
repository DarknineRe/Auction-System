package com.example.project.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.project.model.CommentReaction;

@Repository
public interface CommentReactionRepository extends JpaRepository<CommentReaction, Long> {
    Optional<CommentReaction> findByComment_IdAndUser_Id(Long commentID, Long userID);

    long countByComment_IdAndType(Long commentID, CommentReaction.Type type);
}
