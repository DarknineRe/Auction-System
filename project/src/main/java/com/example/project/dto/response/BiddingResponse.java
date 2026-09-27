package com.example.project.dto.response;

import java.util.Date;
import java.util.List;

public record BiddingResponse(
        Long id,
        List<Long> artworkIds,
        Long ownerId,
        Double startingPrice,
        Double lastBid,
        Date startDate,
        Date endDate) {
}