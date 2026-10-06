package com.example.project.dto.request;

import java.util.Date;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateBiddingRequest(
        @NotEmpty List<@NotNull @Positive Long> artworkIds,
        @NotNull @Positive Double startingPrice,
        @NotNull Date startDate,
        @NotNull Date endDate) {
}
