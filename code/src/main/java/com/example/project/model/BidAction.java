package com.example.project.model;

import java.math.BigDecimal;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
    @Column(precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;
    @ManyToOne //userหลายbidได้
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
    @ManyToOne
    @JoinColumn(name = "bidding_id", referencedColumnName = "id")
    private Bidding bidding;
    @Column
    private Date timestamp;
    public enum Status {
        VALID,
        VOIDED
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) not null default 'VALID'")
    private Status status = Status.VALID;

    @Column
    private Date voidedAt;

    @ManyToOne
    @JoinColumn(name = "voided_by_user_id", referencedColumnName = "id")
    private User voidedBy;

    @Column(length = 500)
    private String voidReason;

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

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setAmount(BigDecimal amount) {
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
     public Status getStatus() {
        return this.status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Date getVoidedAt() {
        return this.voidedAt;
    }

    public void setVoidedAt(Date voidedAt) {
        this.voidedAt = voidedAt;
    }

    public User getVoidedBy() {
        return this.voidedBy;
    }

    public void setVoidedBy(User voidedBy) {
        this.voidedBy = voidedBy;
    }

    public String getVoidReason() {
        return this.voidReason;
    }

    public void setVoidReason(String voidReason) {
        this.voidReason = voidReason;
    }

    
}