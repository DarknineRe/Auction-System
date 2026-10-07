package com.example.project.service.implementation;

import java.math.BigDecimal;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.project.model.Bidding;
import com.example.project.model.Payment;
import com.example.project.model.Sellerprofile;
import com.example.project.model.User;
import com.example.project.repository.BiddingRepository;
import com.example.project.repository.PaymentRepository;
import com.example.project.repository.SellerprofileRepository;
import com.example.project.repository.UserRepository;
import com.example.project.service.SellerprofileService;

@Service
public class SellerprofileServiceImpl implements SellerprofileService {

    private final SellerprofileRepository sellerprofileRepository;
    private final UserRepository userRepository;
    private final BiddingRepository biddingRepository;
    private final PaymentRepository paymentRepository;

    public SellerprofileServiceImpl(
            SellerprofileRepository sellerprofileRepository,
            UserRepository userRepository,
            BiddingRepository biddingRepository,
            PaymentRepository paymentRepository) {
        this.sellerprofileRepository = sellerprofileRepository;
        this.userRepository = userRepository;
        this.biddingRepository = biddingRepository;
        this.paymentRepository = paymentRepository;
    }

    @Override
    @Transactional
    public Sellerprofile createSellerProfile(String email, String bankaccount) {
        validateBankaccount(bankaccount);
        User user = findUserByEmail(email);
        if (sellerprofileRepository.existsByUser_Id(user.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Seller profile already exists");
        }

        Sellerprofile sellerprofile = new Sellerprofile();
        sellerprofile.setUser(user);
        sellerprofile.setBankaccount(bankaccount.trim());
        return sellerprofileRepository.save(sellerprofile);
    }

    @Override
    @Transactional(readOnly = true)
    public Sellerprofile getCurrentSellerProfile(String email) {
        User user = findUserByEmail(email);
        return findSellerProfileByUserId(user.getId());
    }

    @Override
    @Transactional
    public Sellerprofile updateCurrentSellerProfile(String email, String bankaccount) {
        validateBankaccount(bankaccount);
        User user = findUserByEmail(email);
        Sellerprofile sellerprofile = findSellerProfileByUserId(user.getId());
        sellerprofile.setBankaccount(bankaccount.trim());
        return sellerprofileRepository.save(sellerprofile);
    }

    @Override
    @Transactional(readOnly = true)
    public Sellerprofile getSellerProfileById(Long sellerProfileID) {
        return sellerprofileRepository.findById(sellerProfileID)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seller profile not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public Sellerprofile getSellerProfileByUserId(Long userID) {
        return findSellerProfileByUserId(userID);
    }

    @Override
    @Transactional
    public Sellerprofile rateSeller(Long biddingID, Long userID, int score) {
        Bidding bidding = biddingRepository.findByIdForUpdate(biddingID)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bidding not found"));
        if (bidding.getStatus() != Bidding.Status.CLOSED || bidding.getWinner() == null
                || !bidding.getWinner().getId().equals(userID)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only the winner of a closed bidding can rate its seller");
        }
        boolean completed = paymentRepository.findByBidding_Id(biddingID)
                .map(payment -> payment.getStatus() == Payment.Status.COMPLETED)
                .orElse(false);
        if (!completed) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "The seller can only be rated after the payment is completed");
        }
        if (bidding.getSellerRating() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This bidding has already been rated");
        }

        bidding.setSellerRating(score);
        biddingRepository.saveAndFlush(bidding);

        Long sellerUserId = bidding.getOwner().getId();
        Sellerprofile seller = findSellerProfileByUserId(sellerUserId);
        Double average = biddingRepository.averageSellerRatingByOwnerId(sellerUserId);
        sellerprofileRepository.updateRating(seller.getSellprofileId(),
            average == null ? BigDecimal.ZERO : BigDecimal.valueOf(average));
        // The update query clears the persistence context, so read the profile again.
        return findSellerProfileByUserId(sellerUserId);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private Sellerprofile findSellerProfileByUserId(Long userId) {
        return sellerprofileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seller profile not found"));
    }

    private void validateBankaccount(String bankaccount) {
        if (bankaccount == null || bankaccount.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bank account is required");
        }
    }
}
