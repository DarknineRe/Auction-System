package com.example.project.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PlaceBidRequest(
        @NotNull @Positive Long userId,
        @NotNull BigDecimal amount) {
}
