package com.example.project.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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
    private int thumubsup;
    @Column
    private int thumbsdown;
    @OneToOne 
    @JoinColumn (name = "user_id", referencedColumnName = "id")
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
    public int getThumubsup() {
        return this.thumubsup;
    }
    public void setThumubsup(int thumubsup) {
        this.thumubsup = thumubsup;
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
