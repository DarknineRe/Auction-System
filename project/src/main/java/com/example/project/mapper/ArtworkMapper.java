package com.example.project.mapper;

import org.springframework.stereotype.Component;

import com.example.project.domain.entity.Artwork;
import com.example.project.dto.response.ArtworkResponse;

@Component
public class ArtworkMapper {

    public ArtworkResponse toResponse(Artwork artwork) {
        return new ArtworkResponse(
                artwork.getId(),
                artwork.getTitle(),
                artwork.getImageUrl(),
                artwork.getSellerprofile().getSellprofileId());
    }
}