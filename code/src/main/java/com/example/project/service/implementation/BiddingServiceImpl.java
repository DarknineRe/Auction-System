package com.example.project.service.implementation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
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
import com.example.project.repository.SellerprofileRepository;
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
    private final SellerprofileRepository sellerprofileRepository;

    public BiddingServiceImpl(BiddingRepository biddingRepository, ArtworkRepository artworkRepository,
            UserRepository userRepository, BidActionRepository bidActionRepository,
            BiddingStateResolver stateResolver, SellerprofileRepository sellerprofileRepository) {
        this.biddingRepository = biddingRepository;
        this.artworkRepository = artworkRepository;
        this.userRepository = userRepository;
        this.bidActionRepository = bidActionRepository;
        this.stateResolver = stateResolver;
        this.sellerprofileRepository = sellerprofileRepository;
    }

    @Override
    public Bidding createBidding(List<Long> artworkIDs, Long ownerID, BigDecimal startingPrice,
            BigDecimal minimumBidIncrement, Date startDate, Date endDate) {
        validateBidPrices(startingPrice, minimumBidIncrement);
        if (artworkIDs == null || artworkIDs.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A bidding needs at least one artwork.");
        }
        validateDates(startDate, endDate);
        if (!sellerprofileRepository.existsByUser_Id(ownerID)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "A seller profile is required to open a bidding.");
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
        bidding.setMinimumBidIncrement(minimumBidIncrement);
        bidding.setStartDate(startDate);
        bidding.setEndDate(endDate);

        return biddingRepository.save(bidding);
    }

    @Override
    @Transactional(readOnly = true)
    public Bidding getBiddingById(Long biddingID) {
        return biddingRepository.findById(biddingID)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Bidding not found: " + biddingID));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Bidding> getBiddings(Bidding.Status status, Pageable pageable) {
        validateSort(pageable);
        return status == null
                ? biddingRepository.findAll(pageable)
                : biddingRepository.findByStatus(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Bidding> getBiddingsByOwner(Long ownerID, Bidding.Status status, Pageable pageable) {
        validateSort(pageable);
        return status == null
                ? biddingRepository.findByOwner_Id(ownerID, pageable)
                : biddingRepository.findByOwner_IdAndStatus(ownerID, status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Bidding> getBiddingsWonBy(Long winnerID, Pageable pageable) {
        validateSort(pageable);
        return biddingRepository.findByWinner_Id(winnerID, pageable);
    }

    @Override
    @Transactional
    public Bidding updateBidding(Long biddingID, Long ownerID, BigDecimal startingPrice,
            BigDecimal minimumBidIncrement, Date startDate, Date endDate) {
        validateBidPrices(startingPrice, minimumBidIncrement);
        validateDates(startDate, endDate);
        Bidding bidding = findOwnBiddingWithoutBids(biddingID, ownerID, "edited");

        bidding.setStartingPrice(startingPrice);
        bidding.setMinimumBidIncrement(minimumBidIncrement);
        bidding.setStartDate(startDate);
        bidding.setEndDate(endDate);
        return biddingRepository.save(bidding);
    }

    @Override
    @Transactional
    public Bidding cancelBidding(Long biddingID, Long ownerID) {
        Bidding bidding = findOwnBiddingWithoutBids(biddingID, ownerID, "cancelled");
        if (!stateResolver.resolve(bidding.getStatus()).canMoveTo(Bidding.Status.CANCELLED)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot change bidding from " + bidding.getStatus() + " to " + Bidding.Status.CANCELLED);
        }

        bidding.setStatus(Bidding.Status.CANCELLED);
        return biddingRepository.save(bidding);
    }

    // Owners may only change an ACTIVE bidding nobody has bid on yet, so existing bidders are never affected.
    private Bidding findOwnBiddingWithoutBids(Long biddingID, Long ownerID, String action) {
        Bidding bidding = biddingRepository.findByIdForUpdate(biddingID)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Bidding not found: " + biddingID));
        if (bidding.getOwner() == null || !bidding.getOwner().getId().equals(ownerID)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the bidding owner can modify it.");
        }
        if (!stateResolver.resolve(bidding.getStatus()).acceptsBids()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This bidding is " + bidding.getStatus() + " and can no longer be " + action + ".");
        }
        if (bidActionRepository.findTopByBidding_IdAndStatusOrderByAmountDesc(biddingID, BidAction.Status.VALID)
                .isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A bidding that already has bids cannot be " + action + ".");
        }
        return bidding;
    }

    private void validateDates(Date startDate, Date endDate) {
        if (!endDate.after(startDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be after the start date.");
        }
        if (!endDate.after(new Date())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be in the future.");
        }
    }

    private void validateBidPrices(BigDecimal startingPrice, BigDecimal minimumBidIncrement) {
        if (startingPrice == null || startingPrice.signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Starting price must be positive.");
        }
        if (minimumBidIncrement == null || minimumBidIncrement.signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Minimum bid increment must be positive.");
        }
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
    public BidAction placeBid(Long biddingID, String actorEmail, BigDecimal amount) {
        Bidding bidding = biddingRepository.findByIdForUpdate(biddingID)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Bidding not found: " + biddingID));
        User bidder = userRepository.findByEmail(actorEmail.trim().toLowerCase(java.util.Locale.ROOT))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));
        Long userID = bidder.getId();
        
        // Prevent bidding on your own auction
        if (bidding.getOwner().getId().equals(userID)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot bid on your own auction.");
        }

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

        Optional<BidAction> highestBid = bidActionRepository
                .findTopByBidding_IdAndStatusOrderByAmountDesc(biddingID, BidAction.Status.VALID);

        if (highestBid.map(bid -> bid.getUser().getId().equals(userID)).orElse(false)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You are already the highest bidder.");
        }

        // The first bid may match the starting price; later bids must raise the highest bid by the seller's increment.
        BigDecimal minimumBid = highestBid
            .map(bid -> bid.getAmount().add(bidding.getMinimumBidIncrement()))
                .orElse(bidding.getStartingPrice());
        if (amount.compareTo(minimumBid) < 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Bid must be at least " + minimumBid);
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