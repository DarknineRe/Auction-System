package com.example.project.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.TimeUnit;

// Builder pattern: collects the required pieces of a Payment and derives createdAt/dueDate on build().
public class PaymentBuilder {

    private Bidding bidding;
    private User buyer;
    private Sellerprofile sellerprofile;
    private BigDecimal amount;
    private int dueDays;

    public PaymentBuilder bidding(Bidding bidding) {
        this.bidding = bidding;
        return this;
    }

    public PaymentBuilder buyer(User buyer) {
        this.buyer = buyer;
        return this;
    }

    public PaymentBuilder sellerprofile(Sellerprofile sellerprofile) {
        this.sellerprofile = sellerprofile;
        return this;
    }

    public PaymentBuilder amount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public PaymentBuilder dueInDays(int dueDays) {
        this.dueDays = dueDays;
        return this;
    }

    public Payment build() {
        if (bidding == null || buyer == null || sellerprofile == null || amount == null) {
            throw new IllegalStateException("Payment requires bidding, buyer, sellerprofile and amount");
        }
        Date now = new Date();
        Payment payment = new Payment();
        payment.setBidding(bidding);
        payment.setBuyer(buyer);
        payment.setSellerprofile(sellerprofile);
        payment.setAmount(amount);
        payment.setCreatedAt(now);
        payment.setDueDate(new Date(now.getTime() + TimeUnit.DAYS.toMillis(dueDays)));
        return payment;
    }
}
