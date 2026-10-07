package com.example.project.dto.response;

// Seller details anyone may see; leaves out the bank account.
public record PublicSellerprofileResponse(
        Long sellerProfileId,
        Long userId,
        String name,
        double rating,
        int saleCount) {
}
