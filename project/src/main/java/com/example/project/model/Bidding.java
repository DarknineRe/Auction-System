package com.example.project.model;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity 
@Table (name = "Biddings")
public class Bidding {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToMany
    @JoinColumn(name = "artwork_id", referencedColumnName = "id")
    private Artwork artwork;
    @Column 
    private double lastBid;
    @OneToMany
    @JoinColumn(name = "comment_id", referencedColumnName = "id")
    private List<Comment> comments;
    @OneToMany
    @JoinColumn(name = "bidaction_id", referencedColumnName = "id")
    private List<BidAction> bidActions;

    public Bidding() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Artwork getArtwork() {
        return this.artwork;
    }

    public void setArtwork(Artwork artwork) {
        this.artwork = artwork;
    }

    public double getLastBid() {
        return this.lastBid;
    }

    public void setLastBid(double lastBid) {
        this.lastBid = lastBid;
    }

    public List<Comment> getComments() {
        return this.comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public List<BidAction> getBidActions() {
        return this.bidActions;
    }

    public void setBidActions(List<BidAction> bidActions) {
        this.bidActions = bidActions;
    }


    
}
