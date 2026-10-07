package com.example.project.dto.request;

import java.util.Date;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateBiddingRequest(
        @NotNull @Positive Double startingPrice,
        @NotNull Date startDate,
        @NotNull Date endDate) {
}
