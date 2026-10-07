package com.example.project.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

// One payment per closed bidding that has a winner.
@Entity
@Table(name = "Payments")
public class Payment {

    public enum Status {
        AWAITING_PAYMENT,
        PAYMENT_SUBMITTED,
        PAID,
        COMPLETED,
        EXPIRED,
        CANCELLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "bidding_id", referencedColumnName = "id", nullable = false, unique = true)
    private Bidding bidding;

    @ManyToOne(optional = false)
    @JoinColumn(name = "buyer_user_id", referencedColumnName = "id", nullable = false)
    private User buyer;

    // The seller is referenced through the profile so the buyer always sees the current bank account.
    @ManyToOne(optional = false)
    @JoinColumn(name = "seller_profile_id", nullable = false)
    private Sellerprofile sellerprofile;

    // Final price, copied from the winning bid when the bidding closed.
    @Column(nullable = false)
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Status status = Status.AWAITING_PAYMENT;

    @Column(nullable = false)
    private Date createdAt;

    @Column(nullable = false)
    private Date dueDate;

    @Column(length = 2048)
    private String slipUrl;

    @Column
    private Date paidAt;

    @Column
    private Date confirmedAt;

    @Column(length = 500)
    private String rejectReason;

    // Buyer's address as it was when they paid; later profile edits do not change it.
    @Column(length = 1000)
    private String shippingAddress;

    @Column(length = 100)
    private String carrier;

    @Column(length = 100)
    private String trackingNumber;

    @Column(length = 2048)
    private String trackingUrl;

    @Column(length = 1000)
    private String shippingNote;

    @Column
    private Date shippedAt;

    @Column
    private Date completedAt;

    @Column
    private Date cancelledAt;

    @Column(length = 500)
    private String cancelReason;

    public Payment() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Bidding getBidding() {
        return this.bidding;
    }

    public void setBidding(Bidding bidding) {
        this.bidding = bidding;
    }

    public User getBuyer() {
        return this.buyer;
    }

    public void setBuyer(User buyer) {
        this.buyer = buyer;
    }

    public Sellerprofile getSellerprofile() {
        return this.sellerprofile;
    }

    public void setSellerprofile(Sellerprofile sellerprofile) {
        this.sellerprofile = sellerprofile;
    }

    public Double getAmount() {
        return this.amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Status getStatus() {
        return this.status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getDueDate() {
        return this.dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public String getSlipUrl() {
        return this.slipUrl;
    }

    public void setSlipUrl(String slipUrl) {
        this.slipUrl = slipUrl;
    }

    public Date getPaidAt() {
        return this.paidAt;
    }

    public void setPaidAt(Date paidAt) {
        this.paidAt = paidAt;
    }

    public Date getConfirmedAt() {
        return this.confirmedAt;
    }

    public void setConfirmedAt(Date confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public String getRejectReason() {
        return this.rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public String getShippingAddress() {
        return this.shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getCarrier() {
        return this.carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getTrackingNumber() {
        return this.trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getTrackingUrl() {
        return this.trackingUrl;
    }

    public void setTrackingUrl(String trackingUrl) {
        this.trackingUrl = trackingUrl;
    }

    public String getShippingNote() {
        return this.shippingNote;
    }

    public void setShippingNote(String shippingNote) {
        this.shippingNote = shippingNote;
    }

    public Date getShippedAt() {
        return this.shippedAt;
    }

    public void setShippedAt(Date shippedAt) {
        this.shippedAt = shippedAt;
    }

    public Date getCompletedAt() {
        return this.completedAt;
    }

    public void setCompletedAt(Date completedAt) {
        this.completedAt = completedAt;
    }

    public Date getCancelledAt() {
        return this.cancelledAt;
    }

    public void setCancelledAt(Date cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getCancelReason() {
        return this.cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }
}
