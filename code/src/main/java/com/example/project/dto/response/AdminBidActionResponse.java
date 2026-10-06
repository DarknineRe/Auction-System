package com.example.project.dto.response;

import java.util.Date;

import com.example.project.model.BidAction;

public record AdminBidActionResponse(
        Long id,
        Long biddingId,
        Long userId,
        Double amount,
        Date timestamp,
        BidAction.Status status,
        Date voidedAt,
        Long voidedById,
        String voidReason) {
}