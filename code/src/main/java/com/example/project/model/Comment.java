package com.example.project.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
    @ManyToOne
    @JoinColumn (name = "user_id", referencedColumnName = "id")
    private User user;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bidding_id", referencedColumnName = "id", nullable = false)
    private Bidding bidding;
    // Reactions are owned by the comment and removed with it.
    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommentReaction> reactions = new ArrayList<>();

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

    public Bidding getBidding() {
        return this.bidding;
    }

    public void setBidding(Bidding bidding) {
        this.bidding = bidding;
    }
    
}
