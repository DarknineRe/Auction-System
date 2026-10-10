package com.example.project.dto.response;

import java.math.BigDecimal;

public record SellerprofileResponse(
        Long sellerProfileId,
        Long userId,
        String bankaccount,
        BigDecimal rating,
        int saleCount) {
}