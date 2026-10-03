package com.example.project.service.implementation;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.project.model.Artwork;
import com.example.project.model.Sellerprofile;
import com.example.project.repository.ArtworkRepository;
import com.example.project.repository.SellerprofileRepository;
import com.example.project.service.ArtworkService;

@Service
public class ArtworkServiceImpl implements ArtworkService {

    private final ArtworkRepository artworkRepository;
    private final SellerprofileRepository sellerprofileRepository;

    public ArtworkServiceImpl(ArtworkRepository artworkRepository, SellerprofileRepository sellerprofileRepository) {
        this.artworkRepository = artworkRepository;
        this.sellerprofileRepository = sellerprofileRepository;
    }

    @Override
    @Transactional
    public Artwork createArtwork(Long sellerUserID, String title, String imageUrl) {
        Sellerprofile seller = sellerprofileRepository.findByUser_Id(sellerUserID)
                .orElseThrow(() -> new IllegalArgumentException("Seller profile not found for user: " + sellerUserID));

        Artwork artwork = new Artwork();
        artwork.setTitle(title);
        artwork.setImageUrl(imageUrl);
        artwork.setSellerprofile(seller);

        return artworkRepository.save(artwork);
    }

    @Override
    @Transactional(readOnly = true)
    public Artwork getArtworkById(Long artworkID) {
        return artworkRepository.findById(artworkID)
                .orElseThrow(() -> new IllegalArgumentException("Artwork not found: " + artworkID));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Artwork> getAllArtworks(Pageable pageable) {
        return artworkRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Artwork> getArtworksBySeller(Long sellerUserID) {
        return artworkRepository.findBySellerprofile_User_Id(sellerUserID);
    }

    @Override
    @Transactional
    public Artwork updateArtwork(Long artworkID, String title, String imageUrl) {
        Artwork artwork = getArtworkById(artworkID);
        artwork.setTitle(title);
        artwork.setImageUrl(imageUrl);
        return artworkRepository.save(artwork);
    }

    @Override
    @Transactional
    public void deleteArtwork(Long artworkID) {
        if (!artworkRepository.existsById(artworkID)) {
            throw new IllegalArgumentException("Artwork not found: " + artworkID);
        }
        artworkRepository.deleteById(artworkID);
    }
}