package com.example.project.service.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.example.project.model.BidAction;
import com.example.project.model.Bidding;
import com.example.project.repository.BidActionRepository;
import com.example.project.repository.BiddingRepository;
import com.example.project.service.PaymentService;

class BiddingExpiryProcessorTest {

    @Test
    void closesExpiredActiveBiddingUnderRowLock() {
        BiddingRepository biddingRepository = mock(BiddingRepository.class);
        BidActionRepository bidActionRepository = mock(BidActionRepository.class);
        PaymentService paymentService = mock(PaymentService.class);
        Bidding bidding = new Bidding();
        bidding.setId(9L);
        bidding.setStatus(Bidding.Status.ACTIVE);
        bidding.setEndDate(new Date(System.currentTimeMillis() - 1_000));
        when(biddingRepository.findByIdForUpdate(9L)).thenReturn(Optional.of(bidding));
        when(biddingRepository.save(bidding)).thenReturn(bidding);
        BiddingExpiryProcessor processor =
                new BiddingExpiryProcessor(biddingRepository, bidActionRepository, paymentService);

        assertTrue(processor.closeExpiredBidding(9L));

        assertEquals(Bidding.Status.CLOSED, bidding.getStatus());
        verify(bidActionRepository).findTopByBidding_IdAndStatusOrderByAmountDesc(
                9L, BidAction.Status.VALID);
        verify(paymentService).createForClosedBidding(bidding);
    }

    @Test
    void skipsBiddingThatWasAlreadyClosed() {
        BiddingRepository biddingRepository = mock(BiddingRepository.class);
        BidActionRepository bidActionRepository = mock(BidActionRepository.class);
        PaymentService paymentService = mock(PaymentService.class);
        Bidding bidding = new Bidding();
        bidding.setStatus(Bidding.Status.CANCELLED);
        bidding.setEndDate(new Date(System.currentTimeMillis() - 1_000));
        when(biddingRepository.findByIdForUpdate(9L)).thenReturn(Optional.of(bidding));
        BiddingExpiryProcessor processor =
                new BiddingExpiryProcessor(biddingRepository, bidActionRepository, paymentService);

        assertFalse(processor.closeExpiredBidding(9L));

        verify(bidActionRepository, never())
                .findTopByBidding_IdAndStatusOrderByAmountDesc(
                        9L, BidAction.Status.VALID);
        verify(paymentService, never()).createForClosedBidding(bidding);
    }
}
