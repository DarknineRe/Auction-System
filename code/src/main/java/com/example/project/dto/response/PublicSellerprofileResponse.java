package com.example.project.dto.response;

import java.math.BigDecimal;

// Seller details anyone may see; leaves out the bank account.
public record PublicSellerprofileResponse(
        Long sellerProfileId,
        Long userId,
        String name,
        BigDecimal rating,
        int saleCount) {
}
