package com.example.project.service.implementation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.project.model.Artwork;
import com.example.project.model.BidAction;
import com.example.project.model.Bidding;
import com.example.project.model.User;
import com.example.project.repository.ArtworkRepository;
import com.example.project.repository.BiddingRepository;
import com.example.project.repository.UserRepository;
import com.example.project.service.BiddingService;

@Service
public class BiddingServiceImpl implements BiddingService {

    private final BiddingRepository biddingRepository;
    private final ArtworkRepository artworkRepository;
    private final UserRepository userRepository;

    public BiddingServiceImpl(BiddingRepository biddingRepository, ArtworkRepository artworkRepository, UserRepository userRepository) {
        this.biddingRepository = biddingRepository;
        this.artworkRepository = artworkRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Bidding createBidding(List<Long> artworkIDs, Long ownerID, Double startingPrice, Date startDate, Date endDate) {
        if (artworkIDs == null || artworkIDs.isEmpty()) {
            throw new IllegalArgumentException("A bidding needs at least one artwork.");
        }

        List<Artwork> artworks = new ArrayList<>();
        for (Long artworkID : artworkIDs) {
            Artwork artwork = artworkRepository.findById(artworkID)
                    .orElseThrow(() -> new IllegalArgumentException("Artwork not found: " + artworkID));
            artworks.add(artwork);
        }

        User owner = userRepository.findById(ownerID)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + ownerID));

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
                .orElseThrow(() -> new IllegalArgumentException("Bidding not found: " + biddingID));
    }

    @Override
    public List<Bidding> getAllBiddings() {
        return biddingRepository.findAll();
    }

    @Override
    public BidAction placeBid(Long biddingID, Long userID, Double amount) {
        Bidding bidding = getBiddingById(biddingID);
        User bidder = userRepository.findById(userID)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userID));

        if (new Date().after(bidding.getEndDate())) {
            throw new IllegalStateException("This bidding has already closed.");
        }

        double highestSoFar = bidding.getStartingPrice();
        for (BidAction existing : bidding.getBidActions()) {
            if (existing.getAmount() > highestSoFar) {
                highestSoFar = existing.getAmount();
            }
        }

        if (amount <= highestSoFar) {
            throw new IllegalArgumentException(
                    "Bid must be higher than the current price of " + highestSoFar);
        }

        BidAction action = new BidAction();
        action.setBidding(bidding);
        action.setUser(bidder);
        action.setAmount(amount);
        action.setTimestamp(new Date());

        bidding.getBidActions().add(action);
        bidding.setLastBid(amount);

        biddingRepository.save(bidding);
        return action;
    }
}