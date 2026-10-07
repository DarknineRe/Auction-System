package com.example.project.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.project.model.Artwork;

public interface ArtworkService {
    Artwork createArtwork(Long sellerUserID, String title, String imageUrl);

    Artwork getArtworkById(Long artworkID);

    Page<Artwork> getAllArtworks(Pageable pageable);

    List<Artwork> getArtworksBySeller(Long sellerUserID);

    Artwork updateArtwork(Long artworkID, Long sellerUserID, String title, String imageUrl);

    void deleteArtwork(Long artworkID, Long userID);

    void deleteArtworkAsAdmin(Long artworkID);
}