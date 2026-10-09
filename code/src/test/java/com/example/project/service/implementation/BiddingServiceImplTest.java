package com.example.project.service.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.example.project.model.BidAction;
import com.example.project.model.Bidding;
import com.example.project.model.User;
import com.example.project.repository.ArtworkRepository;
import com.example.project.repository.BidActionRepository;
import com.example.project.repository.BiddingRepository;
import com.example.project.repository.SellerprofileRepository;
import com.example.project.repository.UserRepository;
import com.example.project.service.state.BiddingState;
import com.example.project.service.state.BiddingStateResolver;

class BiddingServiceImplTest {

    @Test
    void acceptsBidAtSellerConfiguredIncrement() {
        BidActionRepository bidActionRepository = mock(BidActionRepository.class);
        Bidding bidding = createBidding();
        BiddingServiceImpl service = createService(bidding, bidActionRepository);
        when(bidActionRepository.save(any(BidAction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BidAction savedBid = service.placeBid(7L, "bidder@example.com", new BigDecimal("102.50"));

        assertEquals(new BigDecimal("102.50"), savedBid.getAmount());
        assertEquals(new BigDecimal("102.50"), bidding.getLastBid());
    }

    @Test
    void rejectsBidBelowSellerConfiguredIncrement() {
        BidActionRepository bidActionRepository = mock(BidActionRepository.class);
        BiddingServiceImpl service = createService(createBidding(), bidActionRepository);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.placeBid(7L, "bidder@example.com", new BigDecimal("102.49")));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(bidActionRepository, never()).save(any(BidAction.class));
    }

    @Test
    void acceptsFirstBidAtStartingPrice() {
        BidActionRepository bidActionRepository = mock(BidActionRepository.class);
        Bidding bidding = createBidding();
        bidding.setStartingPrice(new BigDecimal("50.00"));
        BiddingServiceImpl service = createService(
                bidding, bidActionRepository, Optional.empty(), true);
        when(bidActionRepository.save(any(BidAction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BidAction savedBid = service.placeBid(7L, "bidder@example.com", new BigDecimal("50.00"));

        assertEquals(new BigDecimal("50.00"), savedBid.getAmount());
        assertEquals(new BigDecimal("50.00"), bidding.getLastBid());
    }

    @Test
    void rejectsBidOnOwnAuction() {
        BidActionRepository bidActionRepository = mock(BidActionRepository.class);
        BiddingServiceImpl service = createService(
                createBidding(3L), bidActionRepository);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.placeBid(7L, "bidder@example.com", new BigDecimal("102.50")));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(bidActionRepository, never()).save(any(BidAction.class));
    }

    @Test
    void rejectsBidWhenBiddingIsNotAcceptingBids() {
        BidActionRepository bidActionRepository = mock(BidActionRepository.class);
        BiddingServiceImpl service = createService(
                createBidding(), bidActionRepository, Optional.empty(), false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.placeBid(7L, "bidder@example.com", new BigDecimal("100.00")));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(bidActionRepository, never()).save(any(BidAction.class));
    }

    private BiddingServiceImpl createService(Bidding bidding, BidActionRepository bidActionRepository) {
        User previousBidder = new User();
        previousBidder.setId(2L);
        BidAction previousBid = new BidAction();
        previousBid.setAmount(new BigDecimal("100.00"));
        previousBid.setUser(previousBidder);
        return createService(bidding, bidActionRepository, Optional.of(previousBid), true);
    }

    private BiddingServiceImpl createService(
            Bidding bidding,
            BidActionRepository bidActionRepository,
            Optional<BidAction> highestBid,
            boolean acceptsBids) {
        BiddingRepository biddingRepository = mock(BiddingRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        BiddingStateResolver stateResolver = mock(BiddingStateResolver.class);
        BiddingState activeState = mock(BiddingState.class);

        User bidder = new User();
        bidder.setId(3L);

        when(biddingRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(bidding));
        when(biddingRepository.save(bidding)).thenReturn(bidding);
        when(userRepository.findByEmail("bidder@example.com")).thenReturn(Optional.of(bidder));
        when(stateResolver.resolve(Bidding.Status.ACTIVE)).thenReturn(activeState);
        when(activeState.acceptsBids()).thenReturn(acceptsBids);
        when(bidActionRepository.findTopByBidding_IdAndStatusOrderByAmountDesc(
                7L, BidAction.Status.VALID)).thenReturn(highestBid);

        return new BiddingServiceImpl(
                biddingRepository,
                mock(ArtworkRepository.class),
                userRepository,
                bidActionRepository,
                stateResolver,
                mock(SellerprofileRepository.class));
    }

    private Bidding createBidding() {
        return createBidding(1L);
    }

    private Bidding createBidding(Long ownerId) {
        User owner = new User();
        owner.setId(ownerId);
        Bidding bidding = new Bidding();
        bidding.setId(7L);
        bidding.setOwner(owner);
        bidding.setStartingPrice(new BigDecimal("50.00"));
        bidding.setMinimumBidIncrement(new BigDecimal("2.50"));
        bidding.setEndDate(new Date(System.currentTimeMillis() + 60_000));
        return bidding;
    }
}
