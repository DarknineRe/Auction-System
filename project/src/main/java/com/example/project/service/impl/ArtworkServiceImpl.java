package com.example.project.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.project.domain.entity.Artwork;
import com.example.project.domain.entity.Sellerprofile;
import com.example.project.dto.response.ArtworkResponse;
import com.example.project.exception.ResourceNotFoundException;
import com.example.project.mapper.ArtworkMapper;
import com.example.project.repository.ArtworkRepository;
import com.example.project.repository.SellerprofileRepository;
import com.example.project.service.ArtworkService;

@Service
@Transactional(readOnly = true)
public class ArtworkServiceImpl implements ArtworkService {

    private final ArtworkRepository artworkRepository;
    private final SellerprofileRepository sellerprofileRepository;
    private final ArtworkMapper artworkMapper;

    public ArtworkServiceImpl(ArtworkRepository artworkRepository,
                              SellerprofileRepository sellerprofileRepository,
                              ArtworkMapper artworkMapper) {
        this.artworkRepository = artworkRepository;
        this.sellerprofileRepository = sellerprofileRepository;
        this.artworkMapper = artworkMapper;
    }

    @Override
    @Transactional
    public ArtworkResponse create(String title, String imageUrl, Long sellerProfileId) {
        Sellerprofile seller = sellerprofileRepository.findById(sellerProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Sellerprofile", sellerProfileId));

        Artwork artwork = new Artwork();
        artwork.setTitle(title);
        artwork.setImageUrl(imageUrl);
        artwork.setSellerprofile(seller);

        return artworkMapper.toResponse(artworkRepository.save(artwork));
    }

    @Override
    public ArtworkResponse getById(Long id) {
        return artworkMapper.toResponse(findArtwork(id));
    }

    @Override
    public List<ArtworkResponse> getAll() {
        return artworkRepository.findAll().stream()
                .map(artworkMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ArtworkResponse update(Long id, String title, String imageUrl) {
        Artwork artwork = findArtwork(id);
        artwork.setTitle(title);
        artwork.setImageUrl(imageUrl);
        return artworkMapper.toResponse(artwork);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Artwork artwork = findArtwork(id);
        artworkRepository.delete(artwork);
    }

    private Artwork findArtwork(Long id) {
        return artworkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artwork", id));
    }
}