package com.example.project.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
@Entity
@Table (name = "Biddings")
public class Bidding {

    public static final BigDecimal DEFAULT_MINIMUM_BID_INCREMENT = new BigDecimal("1.00");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many-to-many so an artwork keeps its history across biddings (e.g. relisted after a cancellation).
    @ManyToMany
    @JoinTable(name = "bidding_artworks",
            joinColumns = @JoinColumn(name = "bidding_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "artwork_id", referencedColumnName = "id"))
    private List<Artwork> artworks = new ArrayList<>();

    @Column(precision = 19, scale = 4)
    private BigDecimal lastBid;


    @OneToMany(mappedBy = "bidding", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "bidding", cascade = CascadeType.ALL)
    private List<BidAction> bidActions = new ArrayList<>();

    //added bidding owner, price(starting and current)
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User owner;

    @ManyToOne
    @JoinColumn(name = "winner_user_id")
    private User winner;

    @Column
    private Integer sellerRating;

    @Column(precision = 19, scale = 4)
    private BigDecimal startingPrice; // startingPrince and date HERE instead of artwork
    @Column(precision = 19, scale = 4)
    private BigDecimal minimumBidIncrement = DEFAULT_MINIMUM_BID_INCREMENT;
    private Date startDate;
    private Date endDate;

     public enum Status {
        ACTIVE,
        CLOSED,
        CANCELLED
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    
    
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
        // Mutate in place: replacing an orphanRemoval collection breaks Hibernate.
        this.comments.clear();
        if (comments != null) {
            comments.forEach(this::addComment);
        }
    }

    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setBidding(this);
    }

    public void removeComment(Comment comment) {
        comments.remove(comment);
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

    public User getWinner() {
        return this.winner;
    }

    public void setWinner(User winner) {
        this.winner = winner;
    }

    public Integer getSellerRating() {
        return this.sellerRating;
    }

    public void setSellerRating(Integer sellerRating) {
        this.sellerRating = sellerRating;
    }

    public Date getStartDate() {
        return this.startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public BigDecimal getStartingPrice() {
        return this.startingPrice;
    }

    public void setStartingPrice(BigDecimal startingPrice) {
        this.startingPrice = startingPrice;
    }

    public BigDecimal getMinimumBidIncrement() {
        return minimumBidIncrement == null ? DEFAULT_MINIMUM_BID_INCREMENT : minimumBidIncrement;
    }

    public void setMinimumBidIncrement(BigDecimal minimumBidIncrement) {
        this.minimumBidIncrement = minimumBidIncrement;
    }

    public Date getEndDate() {
        return this.endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
    public Status getStatus() {
        return this.status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

}