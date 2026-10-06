package com.example.project.service.implementation;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.project.model.BidAction;
import com.example.project.model.Bidding;
import com.example.project.repository.BidActionRepository;
import com.example.project.repository.BiddingRepository;
import com.example.project.repository.SellerprofileRepository;
import com.example.project.service.BiddingClosingService;

@Service
public class BiddingClosingServiceImpl implements BiddingClosingService {

    private final BiddingRepository biddingRepository;
    private final BidActionRepository bidActionRepository;
    private final SellerprofileRepository sellerprofileRepository;

    public BiddingClosingServiceImpl(BiddingRepository biddingRepository, BidActionRepository bidActionRepository,
            SellerprofileRepository sellerprofileRepository) {
        this.biddingRepository = biddingRepository;
        this.bidActionRepository = bidActionRepository;
        this.sellerprofileRepository = sellerprofileRepository;
    }

    @Override
    @Transactional
    public Bidding close(Bidding bidding) {
        bidActionRepository
                .findTopByBidding_IdAndStatusOrderByAmountDesc(bidding.getId(), BidAction.Status.VALID)
                .ifPresent(topBid -> {
                    bidding.setWinner(topBid.getUser());
                    bidding.setLastBid(topBid.getAmount());
                    recordSale(bidding);
                });
        bidding.setStatus(Bidding.Status.CLOSED);
        return biddingRepository.save(bidding);
    }

    @Override
    @Transactional
    public int closeExpiredBiddings() {
        List<Bidding> expired = biddingRepository.findByStatusAndEndDateBefore(Bidding.Status.ACTIVE, new Date());
        expired.forEach(this::close);
        return expired.size();
    }

    private void recordSale(Bidding bidding) {
        if (bidding.getOwner() == null) {
            return;
        }
        sellerprofileRepository.findByUser_Id(bidding.getOwner().getId()).ifPresent(seller -> {
            seller.setSalecount(seller.getSalecount() + 1);
            sellerprofileRepository.save(seller);
        });
    }
}
