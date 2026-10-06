package com.example.project.service.implementation;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.project.model.Artwork;
import com.example.project.model.Bidding;
import com.example.project.model.Sellerprofile;
import com.example.project.repository.ArtworkRepository;
import com.example.project.repository.BiddingRepository;
import com.example.project.repository.SellerprofileRepository;
import com.example.project.service.ArtworkService;

@Service
public class ArtworkServiceImpl implements ArtworkService {

    private final ArtworkRepository artworkRepository;
    private final SellerprofileRepository sellerprofileRepository;
    private final BiddingRepository biddingRepository;

    public ArtworkServiceImpl(ArtworkRepository artworkRepository, SellerprofileRepository sellerprofileRepository,
            BiddingRepository biddingRepository) {
        this.artworkRepository = artworkRepository;
        this.sellerprofileRepository = sellerprofileRepository;
        this.biddingRepository = biddingRepository;
    }

    @Override
    @Transactional
    public Artwork createArtwork(Long sellerUserID, String title, String imageUrl) {
        Sellerprofile seller = sellerprofileRepository.findByUser_Id(sellerUserID)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Seller profile not found for user: " + sellerUserID));

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
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Artwork not found: " + artworkID));
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
    public Artwork updateArtwork(Long artworkID, Long userID, String title, String imageUrl) {
        Artwork artwork = getArtworkById(artworkID);
        ensureArtworkOwner(artwork, userID);
        artwork.setTitle(title);
        artwork.setImageUrl(imageUrl);
        return artworkRepository.save(artwork);
    }

    @Override
    @Transactional
    public void deleteArtwork(Long artworkID, Long userID) {
        Artwork artwork = getArtworkById(artworkID);
        ensureArtworkOwner(artwork, userID);
        ensureNoBiddingHistory(artworkID);
        artworkRepository.delete(artwork);
    }

    @Override
    @Transactional
    public void deleteArtworkAsAdmin(Long artworkID) {
        Artwork artwork = getArtworkById(artworkID);
        ensureNoBiddingHistory(artworkID);
        artworkRepository.delete(artwork);
    }

    private void ensureArtworkOwner(Artwork artwork, Long userID) {
        Sellerprofile seller = artwork.getSellerprofile();
        if (seller == null || seller.getUser() == null || userID == null
                || !seller.getUser().getId().equals(userID)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the artwork's seller can modify it");
        }
    }

    // Artworks referenced by any bidding are kept so bidding history stays intact.
    private void ensureNoBiddingHistory(Long artworkID) {
        if (biddingRepository.existsByArtworks_IdAndStatus(artworkID, Bidding.Status.ACTIVE)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Artwork is part of an active bidding and cannot be deleted.");
        }
        if (biddingRepository.existsByArtworks_Id(artworkID)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Artwork has bidding history and cannot be deleted.");
        }
    }
}