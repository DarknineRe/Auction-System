package com.example.project.mapper;

import org.springframework.stereotype.Component;

import com.example.project.dto.response.ArtworkResponse;
import com.example.project.model.Artwork;
import com.example.project.model.Sellerprofile;

@Component
public class ArtworkMapper {

    public ArtworkResponse toResponse(Artwork artwork) {
        Sellerprofile seller = artwork.getSellerprofile();
        Long sellerprofileId = seller == null ? null : seller.getSellprofileId();
        Long sellerUserId = (seller == null || seller.getUser() == null) ? null : seller.getUser().getId();

        return new ArtworkResponse(
                artwork.getId(),
                artwork.getTitle(),
                artwork.getImageUrl(),
                sellerprofileId,
                sellerUserId);
    }
}