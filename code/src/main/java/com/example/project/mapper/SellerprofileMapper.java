package com.example.project.mapper;

import org.springframework.stereotype.Component;

import com.example.project.dto.response.PublicSellerprofileResponse;
import com.example.project.dto.response.SellerprofileResponse;
import com.example.project.model.Sellerprofile;

@Component
public class SellerprofileMapper {

    public SellerprofileResponse toResponse(Sellerprofile sellerprofile) {
        return new SellerprofileResponse(
                sellerprofile.getSellprofileId(),
                sellerprofile.getUser().getId(),
                sellerprofile.getBankaccount(),
                sellerprofile.getRating(),
                sellerprofile.getSalecount());
    }

    public PublicSellerprofileResponse toPublicResponse(Sellerprofile sellerprofile) {
        return new PublicSellerprofileResponse(
                sellerprofile.getSellprofileId(),
                sellerprofile.getUser().getId(),
                sellerprofile.getUser().getName(),
                sellerprofile.getRating(),
                sellerprofile.getSalecount());
    }
}