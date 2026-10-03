package com.example.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.project.model.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
}
