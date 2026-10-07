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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
@Entity
@Table (name = "Biddings")
public class Bidding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany
    @JoinColumn(name = "bidding_id", referencedColumnName = "id")
    private List<Artwork> artworks = new ArrayList<>();

    @Column(precision = 19, scale = 4)
    private BigDecimal lastBid;


    @OneToMany(mappedBy = "bidding", cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "bidding", cascade = CascadeType.ALL)
    private List<BidAction> bidActions = new ArrayList<>();

    //added bidding owner, price(starting and current)
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User owner;

    @Column(precision = 19, scale = 4)
    private BigDecimal startingPrice; // startingPrince and date HERE instead of artwork
    private Date startDate;
    private Date endDate;

     public enum Status {
        ACTIVE,
        CLOSED,
        CANCELLED
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) not null default 'ACTIVE'")
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
        this.comments = comments == null ? new ArrayList<>() : comments;
        this.comments.forEach(comment -> comment.setBidding(this));
    }

    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setBidding(this);
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