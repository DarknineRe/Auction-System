package com.example.project.service;

import java.util.List;

import com.example.project.dto.response.ArtworkResponse;

public interface ArtworkService {
    ArtworkResponse create(String title, String imageUrl, Long sellerProfileId);

    ArtworkResponse getById(Long id);

    List<ArtworkResponse> getAll();

    ArtworkResponse update(Long id, String title, String imageUrl);

    void delete(Long id);
}