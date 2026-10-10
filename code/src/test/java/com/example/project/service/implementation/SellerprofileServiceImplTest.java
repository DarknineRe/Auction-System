package com.example.project.service.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.example.project.repository.BiddingRepository;
import com.example.project.repository.PaymentRepository;
import com.example.project.repository.SellerprofileRepository;
import com.example.project.repository.UserRepository;

class SellerprofileServiceImplTest {

    @Test
    void rejectsRatingsOutsideOneToFiveBeforeLoadingBidding() {
        SellerprofileRepository sellerprofileRepository = mock(SellerprofileRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        BiddingRepository biddingRepository = mock(BiddingRepository.class);
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        SellerprofileServiceImpl service = new SellerprofileServiceImpl(
                sellerprofileRepository, userRepository, biddingRepository, paymentRepository);

        for (int score : new int[] {0, 6, 9}) {
            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> service.rateSeller(7L, 2L, score));
            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        }

        verifyNoInteractions(sellerprofileRepository, userRepository, biddingRepository, paymentRepository);
    }
}
