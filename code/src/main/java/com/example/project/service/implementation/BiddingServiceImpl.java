package com.example.project.service.implementation;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.project.model.Artwork;
import com.example.project.model.BidAction;
import com.example.project.model.Bidding;
import com.example.project.model.User;
import com.example.project.repository.ArtworkRepository;
import com.example.project.repository.BidActionRepository;
import com.example.project.repository.BiddingRepository;
import com.example.project.repository.UserRepository;
import com.example.project.service.BiddingService;
import com.example.project.service.state.BiddingStateResolver;

@Service
public class BiddingServiceImpl implements BiddingService {

    private final BiddingRepository biddingRepository;
    private final ArtworkRepository artworkRepository;
    private final UserRepository userRepository;
    private final BidActionRepository bidActionRepository;
    private final BiddingStateResolver stateResolver;

    public BiddingServiceImpl(BiddingRepository biddingRepository, ArtworkRepository artworkRepository,
            UserRepository userRepository, BidActionRepository bidActionRepository,
            BiddingStateResolver stateResolver) {
        this.biddingRepository = biddingRepository;
        this.artworkRepository = artworkRepository;
        this.userRepository = userRepository;
        this.bidActionRepository = bidActionRepository;
        this.stateResolver = stateResolver;
    }

    @Override
    public Bidding createBidding(List<Long> artworkIDs, Long ownerID, Double startingPrice, Date startDate, Date endDate) {
        if (artworkIDs == null || artworkIDs.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A bidding needs at least one artwork.");
        }
        if (!endDate.after(startDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be after the start date.");
        }
        if (!endDate.after(new Date())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be in the future.");
        }

        List<Artwork> artworks = new ArrayList<>();
        for (Long artworkID : new LinkedHashSet<>(artworkIDs)) {
            Artwork artwork = artworkRepository.findById(artworkID)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Artwork not found: " + artworkID));
            if (artwork.getSellerprofile() == null || artwork.getSellerprofile().getUser() == null
                    || !artwork.getSellerprofile().getUser().getId().equals(ownerID)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Artwork " + artworkID + " does not belong to the bidding owner");
            }
            if (biddingRepository.existsByArtworks_IdAndStatus(artworkID, Bidding.Status.ACTIVE)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Artwork " + artworkID + " is already part of an active bidding");
            }
            artworks.add(artwork);
        }

        User owner = userRepository.findById(ownerID)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found: " + ownerID));

        Bidding bidding = new Bidding();
        bidding.setArtworks(artworks);
        bidding.setOwner(owner);
        bidding.setStartingPrice(startingPrice);
        bidding.setLastBid(startingPrice);
        bidding.setStartDate(startDate);
        bidding.setEndDate(endDate);

        return biddingRepository.save(bidding);
    }

    @Override
    public Bidding getBiddingById(Long biddingID) {
        return biddingRepository.findById(biddingID)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Bidding not found: " + biddingID));
    }

    @Override
    public List<Bidding> getAllBiddings() {
        return biddingRepository.findAll();
    }

    @Override
    @Transactional
    public BidAction placeBid(Long biddingID, Long userID, Double amount) {
        Bidding bidding = getBiddingById(biddingID);
        User bidder = userRepository.findById(userID)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found: " + userID));
        if (!stateResolver.resolve(bidding.getStatus()).acceptsBids()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This bidding is " + bidding.getStatus() + " and is not accepting bids.");
        }
        if (bidding.getStartDate() != null && new Date().before(bidding.getStartDate())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This bidding has not started yet.");
        }
        if (new Date().after(bidding.getEndDate())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This bidding has already closed.");
        }

        double highestSoFar = bidActionRepository
                .findTopByBidding_IdAndStatusOrderByAmountDesc(biddingID, BidAction.Status.VALID)
                .map(BidAction::getAmount)
                .orElse(bidding.getStartingPrice());

        if (amount <= highestSoFar) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Bid must be higher than the current price of " + highestSoFar);
        }

        BidAction action = new BidAction();
        action.setBidding(bidding);
        action.setUser(bidder);
        action.setAmount(amount);
        action.setTimestamp(new Date());

        BidAction savedAction = bidActionRepository.save(action);
        bidding.setLastBid(amount);
        biddingRepository.save(bidding);

        return savedAction;
    }
}