package com.example.project.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity 
@Table(name = "Seller_profiles")
public class Sellerprofile {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sellprofileId;
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
    @Column(nullable = false)
    private String bankaccount;
    @Column 
    private double rating;
    @Column 
    private int salecount;

    

    public Sellerprofile() {
    }
    public Long getSellprofileId() {
        return this.sellprofileId;
    }

    public void setSellprofileId(Long sellprofileId) {
        this.sellprofileId = sellprofileId;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getBankaccount() {
        return this.bankaccount;
    }

    public void setBankaccount(String bankaccount) {
        this.bankaccount = bankaccount;
    }

    public double getRating() {
        return this.rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getSalecount() {
        return this.salecount;
    }

    public void setSalecount(int salecount) {
        this.salecount = salecount;
    }



    
}
