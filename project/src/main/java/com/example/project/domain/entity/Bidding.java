package com.example.project.domain.entity;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table (name = "Biddings")
public class Bidding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "bidding_id", referencedColumnName = "id")
    private List<Artwork> artworks = new ArrayList<>();

    @Column(precision = 12, scale = 2)
    private BigDecimal lastBid;


    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "bidding_id", referencedColumnName = "id")
    private List<Comment> comments = new ArrayList<>();

    // เปลี่ยนเป็น mappedBy (มีสองฝั่งพยายามเป็นเจ้าของความสัมพันธ์)
    @OneToMany(mappedBy = "bidding", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BidAction> bidActions = new ArrayList<>();

    //added bidding owner, price(starting and current)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal startingPrice; // startingPrice and date HERE instead of artwork
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public Bidding() {}

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Artwork> getArtworks() {
        return this.artworks;
    }

    public void setArtworks(List<Artwork> artworks) {
        this.artworks = artworks;
    }

    public BigDecimal getLastBid() {
        return this.lastBid;
    }

    public void setLastBid(BigDecimal lastBid) {
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

    public User getOwner() {
    return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public LocalDateTime getStartDate() {
        return this.startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public BigDecimal getStartingPrice() {
        return this.startingPrice;
    }

    public void setStartingPrice(BigDecimal startingPrice) {
        this.startingPrice = startingPrice;
    }

    public LocalDateTime getEndDate() {
        return this.endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

}