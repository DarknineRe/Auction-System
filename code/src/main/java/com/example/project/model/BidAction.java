package com.example.project.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table (name = "Bidactions")
public class BidAction {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;
    @Column
    private double amount;
    @ManyToOne //userหลายbidได้
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
    @ManyToOne
    @JoinColumn(name = "bidding_id", referencedColumnName = "id")
    private Bidding bidding;
    @Column
    private Date timestamp;

    public BidAction() {
    }

    public Bidding getBidding() {
        return this.bidding;
    }

    public void setBidding(Bidding bidding) {
        this.bidding = bidding;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getAmount() {
        return this.amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    
}