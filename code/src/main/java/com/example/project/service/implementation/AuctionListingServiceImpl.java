package com.example.project.service.implementation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.project.dto.request.CreateArtworkRequest;
import com.example.project.dto.request.CreateBiddingRequest;
import com.example.project.model.Artwork;
import com.example.project.model.Bidding;
import com.example.project.service.ArtworkService;
import com.example.project.service.AuctionListingService;
import com.example.project.service.BiddingService;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

@Service
public class AuctionListingServiceImpl implements AuctionListingService {

    private final ArtworkService artworkService;
    private final BiddingService biddingService;
    private final Validator validator;

    public AuctionListingServiceImpl(
            ArtworkService artworkService,
            BiddingService biddingService,
            Validator validator) {
        this.artworkService = artworkService;
        this.biddingService = biddingService;
        this.validator = validator;
    }

    @Override
    @Transactional
    public Bidding createListing(Long sellerUserId, List<String> titles, List<String> imageUrls,
            BigDecimal startingPrice, BigDecimal minimumBidIncrement, Date startDate, Date endDate) {
        List<String> normalizedTitles = normalizeList(titles);
        List<String> normalizedImageUrls = normalizeList(imageUrls);

        if (normalizedTitles.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one artwork title is required");
        }

        if (normalizedTitles.size() != normalizedImageUrls.size() && normalizedImageUrls.size() != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Artwork titles and image URLs must have the same number of entries");
        }

        List<Long> artworkIds = new ArrayList<>();
        for (int i = 0; i < normalizedTitles.size(); i++) {
            String title = normalizedTitles.get(i);
            String imageUrl = i < normalizedImageUrls.size() ? normalizedImageUrls.get(i) : null;
            validate(new CreateArtworkRequest(title, imageUrl));
            Artwork artwork = artworkService.createArtwork(sellerUserId, title, imageUrl);
            artworkIds.add(artwork.getId());
        }

        validate(new CreateBiddingRequest(artworkIds, sellerUserId, startingPrice, minimumBidIncrement, startDate, endDate));
        return biddingService.createBidding(artworkIds, sellerUserId, startingPrice, minimumBidIncrement, startDate, endDate);
    }

    private List<String> normalizeList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> normalized = new ArrayList<>();
        for (String value : values) {
            if (value == null) {
                continue;
            }
            String trimmed = value.trim();
            if (!trimmed.isEmpty()) {
                normalized.add(trimmed);
            }
        }
        return normalized;
    }

    private <T> void validate(T request) {
        var violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .distinct()
                    .reduce((left, right) -> left + ", " + right)
                    .orElse("Invalid request");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }
}
