package com.example.project.service.implementation;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

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
    public Bidding createListing(Long sellerUserId, String title, String imageUrl,
            BigDecimal startingPrice, Date startDate, Date endDate) {
        validate(new CreateArtworkRequest(title, imageUrl));
        Artwork artwork = artworkService.createArtwork(sellerUserId, title, imageUrl);
        validate(new CreateBiddingRequest(
                List.of(artwork.getId()), sellerUserId, startingPrice, startDate, endDate));
        return biddingService.createBidding(
                List.of(artwork.getId()), sellerUserId, startingPrice, startDate, endDate);
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
