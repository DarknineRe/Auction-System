package com.example.project.dto.response;

import java.util.Date;

public record BidActionResponse(
        Long id,
        Long biddingId,
        Long userId,
        Double amount,
        Date timestamp) {
}