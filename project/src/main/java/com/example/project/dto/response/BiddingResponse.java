package com.example.project.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record BiddingResponse(
        Long id,
        Long ownerId,
        BigDecimal startingPrice,
        BigDecimal lastBid,
        LocalDateTime startDate,
        LocalDateTime endDate,
        List<Long> artworkIds) {
}