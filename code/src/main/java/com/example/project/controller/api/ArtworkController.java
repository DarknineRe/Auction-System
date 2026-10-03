package com.example.project.controller.api;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.project.dto.request.CreateArtworkRequest;
import com.example.project.dto.request.UpdateArtworkRequest;
import com.example.project.dto.response.ArtworkResponse;
import com.example.project.mapper.ArtworkMapper;
import com.example.project.model.Artwork;
import com.example.project.service.ArtworkService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/artworks")
public class ArtworkController {

    private final ArtworkService artworkService;
    private final ArtworkMapper artworkMapper;

    public ArtworkController(ArtworkService artworkService, ArtworkMapper artworkMapper) {
        this.artworkService = artworkService;
        this.artworkMapper = artworkMapper;
    }

    @PostMapping
    public ResponseEntity<ArtworkResponse> createArtwork(@Valid @RequestBody CreateArtworkRequest request) {
        Artwork artwork = artworkService.createArtwork(
                request.sellerUserId(),
                request.title(),
                request.imageUrl());

        return ResponseEntity.status(HttpStatus.CREATED).body(artworkMapper.toResponse(artwork));
    }

    @GetMapping
    public ResponseEntity<PagedModel<ArtworkResponse>> getAllArtworks(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        Page<ArtworkResponse> page = artworkService.getAllArtworks(pageable)
                .map(artworkMapper::toResponse);

        return ResponseEntity.ok(new PagedModel<>(page));
    }

    @GetMapping("/{artworkId}")
    public ResponseEntity<ArtworkResponse> getArtworkById(@PathVariable Long artworkId) {
        return ResponseEntity.ok(artworkMapper.toResponse(artworkService.getArtworkById(artworkId)));
    }

    @GetMapping("/seller/{sellerUserId}")
    public ResponseEntity<List<ArtworkResponse>> getArtworksBySeller(@PathVariable Long sellerUserId) {
        return ResponseEntity.ok(artworkService.getArtworksBySeller(sellerUserId).stream()
                .map(artworkMapper::toResponse)
                .collect(Collectors.toList()));
    }

    @PutMapping("/{artworkId}")
    public ResponseEntity<ArtworkResponse> updateArtwork(
            @PathVariable Long artworkId,
            @Valid @RequestBody UpdateArtworkRequest request) {
        Artwork artwork = artworkService.updateArtwork(artworkId, request.title(), request.imageUrl());

        return ResponseEntity.ok(artworkMapper.toResponse(artwork));
    }

    @DeleteMapping("/{artworkId}")
    public ResponseEntity<Void> deleteArtwork(@PathVariable Long artworkId) {
        artworkService.deleteArtwork(artworkId);

        return ResponseEntity.noContent().build();
    }
}