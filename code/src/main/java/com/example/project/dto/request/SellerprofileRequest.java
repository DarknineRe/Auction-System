package com.example.project.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SellerprofileRequest(
        @NotBlank String bankaccount) {
}