package com.example.project.service.implementation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
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

    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "startingPrice", "lastBid", "startDate", "endDate", "status", "owner.id", "owner.name", "owner.email");

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
    public Bidding createBidding(List<Long> artworkIDs, Long ownerID, BigDecimal startingPrice, Date startDate, Date endDate) {
        if (artworkIDs == null || artworkIDs.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A bidding needs at least one artwork.");
        }

        // Validate dates
        Date now = new Date();
        if (startDate.before(now)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date cannot be in the past.");
        }
        if (endDate.before(startDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be after start date.");
        }

        // Check for duplicate artworks in the request
        if (new java.util.HashSet<>(artworkIDs).size() != artworkIDs.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicate artwork IDs in request.");
        }

        List<Artwork> artworks = new ArrayList<>();
        for (Long artworkID : artworkIDs) {
            Artwork artwork = artworkRepository.findById(artworkID)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Artwork not found: " + artworkID));
            
            // Check if artwork is already in an ACTIVE bidding
            if (biddingRepository.existsByArtworks_IdAndStatus(artworkID, Bidding.Status.ACTIVE)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Artwork " + artworkID + " is already in an active bidding.");
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
    @Transactional(readOnly = true)
    public Page<Bidding> getAllBiddings(Pageable pageable) {
        validateSort(pageable);
        return biddingRepository.findAll(pageable);
    }

    private void validateSort(Pageable pageable) {
        for (Sort.Order order : pageable.getSort()) {
            if (!SORTABLE_FIELDS.contains(order.getProperty())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Cannot sort by: " + order.getProperty());
            }
        }
    }

    @Override
    @Transactional
    public BidAction placeBid(Long biddingID, Long userID, BigDecimal amount) {
        Bidding bidding = getBiddingById(biddingID);
        User bidder = userRepository.findById(userID)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found: " + userID));
        
        // Prevent bidding on your own auction
        if (bidding.getOwner().getId().equals(userID)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot bid on your own auction.");
        }

        if (!stateResolver.resolve(bidding.getStatus()).acceptsBids()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This bidding is " + bidding.getStatus() + " and is not accepting bids.");
        }
        Date now = new Date();
        if (now.before(bidding.getStartDate())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This bidding has not started yet.");
        }
        if (now.after(bidding.getEndDate())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This bidding has already closed.");
        }

        BigDecimal highestSoFar = bidActionRepository
                .findTopByBidding_IdAndStatusOrderByAmountDesc(biddingID, BidAction.Status.VALID)
                .map(BidAction::getAmount)
                .orElse(bidding.getStartingPrice());

        if (amount.compareTo(highestSoFar) <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Bid must be higher than the current price of " + highestSoFar);
        }

        // Prevent self-bidding (bidding against your own previous bid)
        BidAction highestBid = bidActionRepository
                .findTopByBidding_IdAndStatusOrderByAmountDesc(biddingID, BidAction.Status.VALID)
                .orElse(null);
        if (highestBid != null && highestBid.getUser().getId().equals(userID)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You are already the highest bidder.");
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