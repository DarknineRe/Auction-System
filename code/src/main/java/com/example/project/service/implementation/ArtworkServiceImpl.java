package com.example.project.service.implementation;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.project.model.Artwork;
import com.example.project.model.Sellerprofile;
import com.example.project.repository.ArtworkRepository;
import com.example.project.repository.SellerprofileRepository;
import com.example.project.service.ArtworkService;

@Service
public class ArtworkServiceImpl implements ArtworkService {

    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "title", "imageUrl", "sellerprofile.sellprofileId", "sellerprofile.user.id", "sellerprofile.user.name");

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
        validateSort(pageable);
        return artworkRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Artwork> getArtworksBySeller(Long sellerUserID) {
        return artworkRepository.findBySellerprofile_User_Id(sellerUserID);
    }

    @Override
    @Transactional
    public Artwork updateArtwork(Long artworkID, String actorEmail, String title, String imageUrl) {
        Artwork artwork = getArtworkById(artworkID);
        if (artwork.getSellerprofile() == null
                || artwork.getSellerprofile().getUser() == null
                || !artwork.getSellerprofile().getUser().getEmail().equalsIgnoreCase(actorEmail.trim())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the artwork owner can update it");
        }
        artwork.setTitle(title);
        artwork.setImageUrl(imageUrl);
        return artworkRepository.save(artwork);
    }

    @Override
    @Transactional
    public void deleteArtwork(Long artworkID) {
        if (!artworkRepository.existsById(artworkID)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Artwork not found: " + artworkID);
        }
        artworkRepository.deleteById(artworkID);
    }

    private void validateSort(Pageable pageable) { // throw status500 if bad sort
        for (Sort.Order order : pageable.getSort()) {
            if (!SORTABLE_FIELDS.contains(order.getProperty())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Cannot sort by: " + order.getProperty());
            }
        }
    }
}