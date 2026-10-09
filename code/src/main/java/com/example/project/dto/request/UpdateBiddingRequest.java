package com.example.project.dto.request;

import java.math.BigDecimal;
import java.util.Date;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateBiddingRequest(
        @NotNull @Positive BigDecimal startingPrice,
        @NotNull @Positive BigDecimal minimumBidIncrement,
        @NotNull Date startDate,
        @NotNull Date endDate) {
}
