package com.example.project.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CreateBiddingRequest(
        @NotEmpty(message = "artworkIds must not be empty")
        List<Long> artworkIds,

        @NotNull(message = "ownerId is required")
        Long ownerId,

        @NotNull(message = "startingPrice is required")
        @DecimalMin(value = "0.01", message = "startingPrice must be at least 0.01")
        BigDecimal startingPrice,

        @NotNull(message = "startDate is required")
        LocalDateTime startDate,

        @NotNull(message = "endDate is required")
        LocalDateTime endDate) {
}