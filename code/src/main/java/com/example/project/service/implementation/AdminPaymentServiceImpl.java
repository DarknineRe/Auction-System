package com.example.project.service.implementation;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.project.model.Bidding;
import com.example.project.model.Payment;
import com.example.project.repository.BiddingRepository;
import com.example.project.repository.PaymentRepository;
import com.example.project.repository.SellerprofileRepository;
import com.example.project.service.AdminPaymentService;
import com.example.project.service.state.PaymentStateResolver;

@Service
public class AdminPaymentServiceImpl implements AdminPaymentService {

    private final PaymentRepository paymentRepository;
    private final BiddingRepository biddingRepository;
    private final SellerprofileRepository sellerprofileRepository;
    private final PaymentStateResolver stateResolver;

    public AdminPaymentServiceImpl(PaymentRepository paymentRepository,
            BiddingRepository biddingRepository,
            SellerprofileRepository sellerprofileRepository,
            PaymentStateResolver stateResolver) {
        this.paymentRepository = paymentRepository;
        this.biddingRepository = biddingRepository;
        this.sellerprofileRepository = sellerprofileRepository;
        this.stateResolver = stateResolver;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Payment> getPayments(Payment.Status status, Pageable pageable) {
        PaymentServiceImpl.validateSort(pageable);
        return status == null
                ? paymentRepository.findAll(pageable)
                : paymentRepository.findByStatus(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Payment getPayment(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> notFound(paymentId));
    }

    // Also used for disputes after completion: the sale and its seller rating are taken back.
    @Override
    @Transactional
    public Payment cancelPayment(Long paymentId, String reason) {
        // Lock the bidding first, the same order rateSeller uses, so a rating cannot slip in mid-cancel.
        Long biddingId = paymentRepository.findBiddingIdById(paymentId)
                .orElseThrow(() -> notFound(paymentId));
        Bidding bidding = biddingRepository.findByIdForUpdate(biddingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Bidding not found: " + biddingId));
        Payment payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> notFound(paymentId));

        if (!stateResolver.resolve(payment.getStatus()).canMoveTo(Payment.Status.CANCELLED)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot change payment from " + payment.getStatus() + " to " + Payment.Status.CANCELLED);
        }
        boolean wasCompleted = payment.getStatus() == Payment.Status.COMPLETED;

        payment.setStatus(Payment.Status.CANCELLED);
        payment.setCancelledAt(new Date());
        payment.setCancelReason(reason.trim());
        Payment saved = paymentRepository.save(payment);

        if (wasCompleted) {
            Long sellerProfileId = saved.getSellerprofile().getSellprofileId();
            if (bidding.getSellerRating() != null) {
                bidding.setSellerRating(null);
                biddingRepository.saveAndFlush(bidding);
                Double average = biddingRepository.averageSellerRatingByOwnerId(bidding.getOwner().getId());
                sellerprofileRepository.updateRating(sellerProfileId,
                    average == null ? BigDecimal.ZERO : BigDecimal.valueOf(average));
            }
            sellerprofileRepository.addToSalecount(sellerProfileId, -1);
        }
        return saved;
    }

    private ResponseStatusException notFound(Long paymentId) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found: " + paymentId);
    }
}
