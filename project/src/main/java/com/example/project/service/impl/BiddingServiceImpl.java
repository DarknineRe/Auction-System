package com.example.project.service.impl;

import java.util.ArrayList;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.project.domain.entity.Artwork;
import com.example.project.domain.entity.BidAction;
import com.example.project.domain.entity.Bidding;
import com.example.project.domain.entity.User;
import com.example.project.dto.response.BidActionResponse;
import com.example.project.dto.response.BiddingResponse;
import com.example.project.mapper.BidActionMapper;
import com.example.project.mapper.BiddingMapper;
import com.example.project.repository.BidActionRepository;
import com.example.project.exception.BusinessRuleException;
import com.example.project.exception.ResourceNotFoundException;
import com.example.project.repository.ArtworkRepository;
import com.example.project.repository.BiddingRepository;
import com.example.project.repository.UserRepository;
import com.example.project.service.BiddingService;

@Service
@Transactional(readOnly = true)
public class BiddingServiceImpl implements BiddingService {

    private final BiddingRepository biddingRepository;
    private final ArtworkRepository artworkRepository;
    private final UserRepository userRepository;
    private final BidActionRepository bidActionRepository;
    private final BiddingMapper biddingMapper;
    private final BidActionMapper bidActionMapper;

    public BiddingServiceImpl(BiddingRepository biddingRepository,
                            ArtworkRepository artworkRepository,
                            UserRepository userRepository,
                            BidActionRepository bidActionRepository,
                            BiddingMapper biddingMapper,
                            BidActionMapper bidActionMapper) {
        this.biddingRepository = biddingRepository;
        this.artworkRepository = artworkRepository;
        this.userRepository = userRepository;
        this.bidActionRepository = bidActionRepository;
        this.biddingMapper = biddingMapper;
        this.bidActionMapper = bidActionMapper;
    }

    @Override
    @Transactional
    public BiddingResponse createBidding(List<Long> artworkIDs, Long ownerID, BigDecimal startingPrice, LocalDateTime startDate, LocalDateTime endDate) {
        if (artworkIDs == null || artworkIDs.isEmpty()) {
            throw new BusinessRuleException("A bidding needs at least one artwork.");
        }

        List<Artwork> artworks = new ArrayList<>();
        for (Long artworkID : artworkIDs) {
            Artwork artwork = artworkRepository.findById(artworkID)
                    .orElseThrow(() -> new ResourceNotFoundException("Artwork", artworkID));
            artworks.add(artwork);
        }

        User owner = userRepository.findById(ownerID)
                .orElseThrow(() -> new ResourceNotFoundException("User", ownerID));

        Bidding bidding = new Bidding();
        bidding.setArtworks(artworks);
        bidding.setOwner(owner);
        bidding.setStartingPrice(startingPrice);
        bidding.setLastBid(startingPrice);
        bidding.setStartDate(startDate);
        bidding.setEndDate(endDate);

        return biddingMapper.toResponse(biddingRepository.save(bidding));
    }

    @Override
    public BiddingResponse getBiddingById(Long biddingID) {
        return biddingMapper.toResponse(findBidding(biddingID));
    }

    @Override
    public List<BiddingResponse> getAllBiddings() {
        return biddingRepository.findAll().stream()
                .map(biddingMapper::toResponse)
                .toList();
    }

    // เมธอด private ใหม่ ใช้ภายในที่ต้องการ Entity
    private Bidding findBidding(Long biddingID) {
        return biddingRepository.findById(biddingID)
                .orElseThrow(() -> new ResourceNotFoundException("Bidding", biddingID));
    }
    @Override
    @Transactional
    public BidActionResponse placeBid(Long biddingID, Long userID, BigDecimal amount) {
        Bidding bidding = findBidding(biddingID);
        User bidder = userRepository.findById(userID)
                .orElseThrow(() -> new ResourceNotFoundException("User", userID));

        if (LocalDateTime.now().isAfter(bidding.getEndDate())) {
            throw new BusinessRuleException("This bidding has already closed.");
        }

        BigDecimal highestSoFar = bidding.getStartingPrice();
        for (BidAction existing : bidding.getBidActions()) {
            if (existing.getAmount().compareTo(highestSoFar) > 0) {
                highestSoFar = existing.getAmount();
            }
        }

        if (amount.compareTo(highestSoFar) <= 0) {
            throw new BusinessRuleException(
                "Bid must be higher than the current price of " + highestSoFar);
        }

        BidAction action = new BidAction();
        action.setBidding(bidding);
        action.setUser(bidder);
        action.setAmount(amount);
        action.setTimestamp(LocalDateTime.now());

        BidAction saved = bidActionRepository.save(action);
        bidding.getBidActions().add(saved);
        bidding.setLastBid(amount);

        return bidActionMapper.toResponse(saved);
    }
}