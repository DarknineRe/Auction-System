package com.example.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SubmitPaymentSlipRequest(
        @NotBlank @Size(max = 2048)
        @Pattern(regexp = "(?i)^https?://\\S+$", message = "must be an http(s) URL") String slipUrl) {
}
