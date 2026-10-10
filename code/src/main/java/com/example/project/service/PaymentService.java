package com.example.project.service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.project.model.Bidding;
import com.example.project.model.Payment;

public interface PaymentService {
    void createForClosedBidding(Bidding bidding);

    Payment getPayment(Long paymentID, Long userID);

    Page<Payment> getPurchases(Long buyerID, Payment.Status status, Pageable pageable);

    List<Payment> getPurchasesForBiddings(Long buyerID, Collection<Long> biddingIDs);

    // Empty when the bidding has no payment or the user is neither its buyer nor its seller.
    Optional<Payment> findPaymentForParticipant(Long biddingID, Long userID);

    Page<Payment> getSales(Long sellerUserID, Payment.Status status, Pageable pageable);

    Payment submitSlip(Long paymentID, Long buyerID, String slipUrl);

    Payment confirmPayment(Long paymentID, Long sellerUserID);

    Payment rejectPayment(Long paymentID, Long sellerUserID, String reason);

    Payment ship(Long paymentID, Long sellerUserID, String carrier, String trackingNumber,
            String trackingUrl, String shippingNote);

    int expireOverduePayments();
}
