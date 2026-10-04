package com.example.project.dto.response;

public record SellerprofileResponse(
        Long sellerProfileId,
        Long userId,
        String bankaccount,
        double rating,
        int saleCount) {
}