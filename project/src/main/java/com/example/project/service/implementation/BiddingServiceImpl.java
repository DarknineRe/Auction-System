package com.example.project.service.implementation;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.project.model.*;
import com.example.project.repository.*;
import com.example.project.service.*;

@Service
public class BiddingServiceImpl implements BiddingService {

    private final BiddingRepository biddingRepository;
    private final ArtworkRepository artworkRepository;
    private final UserRepository userRepository;

    // Constructor injection (no @Autowired needed with a single constructor)
    public BiddingServiceImpl(BiddingRepository biddingRepository,ArtworkRepository artworkRepository,UserRepository userRepository) {
        this.biddingRepository = biddingRepository;
        this.artworkRepository = artworkRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Bidding createBidding(Long artworkID, Long ownerID, Double startingPrice,LocalDate startDate, LocalDate endDate) {
        Artwork artwork = artworkRepository.findById(artworkID)
                .orElseThrow(() -> new IllegalArgumentException("Artwork not found: " + artworkID));
        User owner = userRepository.findById(ownerID)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + ownerID));
 
        Bidding bidding = new Bidding();
        bidding.setArtwork(artwork);
        bidding.setOwner(owner);
        bidding.setStarting(startingPrice);
        bidding.setPrice(startingPrice); // current price = starting price
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

        if (LocalDate.now().isAfter(bidding.getEndDate())) {
            throw new IllegalStateException("This bidding has already closed.");
        }

        // Business rule: a new bid must beat every previous bid, not just the
        // cached "price" field, in case that field ever gets out of sync.
        double highestSoFar = bidding.getStarting();
        for (BidAction existing : bidding.getBidActions()) {
            if (existing.getAmount() > highestSoFar) {
                highestSoFar = existing.getAmount();
            }
        }

        if (amount <= highestSoFar) {
            throw new IllegalArgumentException(
                    "Bid must be higher than the current price of " + highestSoFar);
        }

        BidAction action = new BidAction(bidding, bidder, amount, LocalDate.now());
        bidding.getBidActions().add(action);
        bidding.setPrice(amount); // keep the cached current price up to date

        biddingRepository.save(bidding);
        return action;
    }
}
