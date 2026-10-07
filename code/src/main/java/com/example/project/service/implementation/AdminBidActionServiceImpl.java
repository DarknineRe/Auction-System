package com.example.project.service.implementation;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.project.model.BidAction;
import com.example.project.model.Bidding;
import com.example.project.model.User;
import com.example.project.repository.BidActionRepository;
import com.example.project.repository.BiddingRepository;
import com.example.project.repository.UserRepository;
import com.example.project.service.AdminBidActionService;
import com.example.project.service.state.BiddingStateResolver;

@Service
public class AdminBidActionServiceImpl implements AdminBidActionService {

    private final BidActionRepository bidActionRepository;
    private final BiddingRepository biddingRepository;
    private final UserRepository userRepository;
    private final BiddingStateResolver stateResolver;

    public AdminBidActionServiceImpl(BidActionRepository bidActionRepository,
            BiddingRepository biddingRepository,
            UserRepository userRepository,
            BiddingStateResolver stateResolver) {
        this.bidActionRepository = bidActionRepository;
        this.biddingRepository = biddingRepository;
        this.userRepository = userRepository;
        this.stateResolver = stateResolver;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BidAction> getAllBids(Long biddingId) {
        if (!biddingRepository.existsById(biddingId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bidding not found: " + biddingId);
        }
        return bidActionRepository.findByBidding_IdOrderByAmountDesc(biddingId);
    }

    @Override
    @Transactional
    public BidAction voidBid(String actorEmail, Long bidId, String reason) {
        // Lock the bidding before loading the bid, so both are read fresh under the lock.
        Long biddingId = bidActionRepository.findBiddingIdById(bidId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Bid not found: " + bidId));
        Bidding bidding = biddingRepository.findByIdForUpdate(biddingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Bidding not found: " + biddingId));
        BidAction bid = bidActionRepository.findById(bidId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Bid not found: " + bidId));

        if (bid.getStatus() == BidAction.Status.VOIDED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Bid is already voided: " + bidId);
        }
        if (!stateResolver.resolve(bidding.getStatus()).acceptsBids()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Bids can only be voided while the bidding is ACTIVE, but it is " + bidding.getStatus());
        }

        User actor = userRepository.findByEmail(actorEmail.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found: " + actorEmail));

        bid.setStatus(BidAction.Status.VOIDED);
        bid.setVoidedAt(new Date());
        bid.setVoidedBy(actor);
        bid.setVoidReason(reason.trim());
        BidAction saved = bidActionRepository.saveAndFlush(bid);

        BigDecimal currentPrice = bidActionRepository
                .findTopByBidding_IdAndStatusOrderByAmountDesc(bidding.getId(), BidAction.Status.VALID)
                .map(BidAction::getAmount)
                .orElse(null);
        bidding.setLastBid(highestValidBid);
        biddingRepository.save(bidding);

        return saved;
    }
}
