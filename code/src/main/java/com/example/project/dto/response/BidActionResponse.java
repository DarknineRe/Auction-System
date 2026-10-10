package com.example.project.dto.response;

import java.math.BigDecimal;
import java.util.Date;

public record BidActionResponse(
        Long id,
        Long biddingId,
        Long userId,
        BigDecimal amount,
        Date timestamp) {
}