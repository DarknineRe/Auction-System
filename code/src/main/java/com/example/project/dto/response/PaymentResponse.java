package com.example.project.dto.response;

import java.util.Date;

import com.example.project.model.Payment;

public record PaymentResponse(
        Long id,
        Long biddingId,
        Long buyerId,
        Long sellerUserId,
        Long sellerProfileId,
        String sellerBankAccount,
        Double amount,
        Payment.Status status,
        Date createdAt,
        Date dueDate,
        String slipUrl,
        Date paidAt,
        Date confirmedAt,
        String rejectReason,
        String shippingAddress,
        String carrier,
        String trackingNumber,
        String trackingUrl,
        String shippingNote,
        Date shippedAt,
        Date completedAt,
        Date cancelledAt,
        String cancelReason) {
}
