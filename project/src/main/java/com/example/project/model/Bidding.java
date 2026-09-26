package com.example.project.model;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "bidding_id", referencedColumnName = "id")
    private List<Artwork> artworks = new ArrayList<>();

    @Column
    private double lastBid;


    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "bidding_id", referencedColumnName = "id")
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "bidding_id", referencedColumnName = "id")
    private List<BidAction> bidActions = new ArrayList<>();

    //added bidding owner, price(starting and current)
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User owner;

    private Double startingPrice; // startingPrince and date HERE instead of artwork
    private Date startDate;
    private Date endDate;

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

    public double getStartingPrice() {
        return this.startingPrice;
    }

    public void setStartingPrice(double startingPrice) {
        this.startingPrice = startingPrice;
    }

    public Date getEndDate() {
        return this.endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

}