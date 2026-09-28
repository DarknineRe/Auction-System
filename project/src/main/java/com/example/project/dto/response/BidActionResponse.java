package com.example.project.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BidActionResponse(
        Long id,
        Long biddingId,
        Long userId,
        BigDecimal amount,
        LocalDateTime timestamp) {
}