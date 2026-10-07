package com.example.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ShipPaymentRequest(
        @NotBlank @Size(max = 100) String carrier,
        @NotBlank @Size(max = 100) String trackingNumber,
        // Only http(s) links, so the buyer's page never renders e.g. a javascript: URL.
        @NotBlank @Size(max = 2048)
        @Pattern(regexp = "(?i)^https?://\\S+$", message = "must be an http(s) URL") String trackingUrl,
        @Size(max = 1000) String shippingNote) {
}
