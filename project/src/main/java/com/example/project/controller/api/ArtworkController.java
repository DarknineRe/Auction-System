package com.example.project.controller.api;

import java.util.List;

import jakarta.validation.Valid;

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
import com.example.project.service.ArtworkService;

@RestController
@RequestMapping("/api/v1/artworks")
public class ArtworkController {

    private final ArtworkService artworkService;

    public ArtworkController(ArtworkService artworkService) {
        this.artworkService = artworkService;
    }

    @PostMapping
    public ResponseEntity<ArtworkResponse> create(@Valid @RequestBody CreateArtworkRequest request) {
        ArtworkResponse response = artworkService.create(
                request.title(), request.imageUrl(), request.sellerProfileId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<ArtworkResponse> getAll() {
        return artworkService.getAll();
    }

    @GetMapping("/{id}")
    public ArtworkResponse getOne(@PathVariable Long id) {
        return artworkService.getById(id);
    }

    @PutMapping("/{id}")
    public ArtworkResponse update(@PathVariable Long id, @Valid @RequestBody UpdateArtworkRequest request) {
        return artworkService.update(id, request.title(), request.imageUrl());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        artworkService.delete(id);
        return ResponseEntity.noContent().build();
    }
}