package com.example.project.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table (name = "Comments")
public class Comment {
    @Id
    @GeneratedValue (strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String message;
    @Column
    private int thumbsup;
    @Column
    private int thumbsdown;

    // 1 user comment ได้มากกว่า 1 comment
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    public Comment() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public int getThumbsup() {
        return this.thumbsup;
    }
    public void setThumbsup(int thumbsup) {
        this.thumbsup = thumbsup;
    }
    public int getThumbsdown() {
        return this.thumbsdown;
    }
    public void setThumbsdown(int thumbsdown) {
        this.thumbsdown = thumbsdown;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    
}
