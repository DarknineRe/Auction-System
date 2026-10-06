package com.example.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VoidBidRequest(@NotBlank @Size(max = 500) String reason) {
}