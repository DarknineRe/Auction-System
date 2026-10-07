package com.example.project.dto.response;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.example.project.model.Bidding;

public record BiddingResponse(
        Long id,
        List<Long> artworkIds,
        Long ownerId,
        BigDecimal startingPrice,
        BigDecimal lastBid,
        Date startDate,
        Date endDate,
        Bidding.Status status,
        Long winnerId,
        Integer sellerRating) {
}