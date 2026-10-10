package com.example.project.service.implementation;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.project.model.Bidding;
import com.example.project.model.Payment;
import com.example.project.model.Sellerprofile;
import com.example.project.repository.PaymentRepository;
import com.example.project.repository.SellerprofileRepository;
import com.example.project.service.PaymentService;
import com.example.project.service.state.PaymentStateResolver;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    static final Set<String> SORTABLE_FIELDS =
            Set.of("id", "amount", "status", "createdAt", "dueDate", "completedAt");
    // A buyer whose slip was rejected always gets at least this long to send a new one.
    private static final long RESUBMIT_GRACE_MS = TimeUnit.DAYS.toMillis(1);

    private final PaymentRepository paymentRepository;
    private final SellerprofileRepository sellerprofileRepository;
    private final PaymentStateResolver stateResolver;
    private final int dueDays;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
            SellerprofileRepository sellerprofileRepository,
            PaymentStateResolver stateResolver,
            @Value("${app.payment.due-days:3}") int dueDays) {
        this.paymentRepository = paymentRepository;
        this.sellerprofileRepository = sellerprofileRepository;
        this.stateResolver = stateResolver;
        this.dueDays = dueDays;
    }

    @Override
    @Transactional
    public void createForClosedBidding(Bidding bidding) {
        if (bidding.getWinner() == null || bidding.getLastBid() == null
                || paymentRepository.existsByBidding_Id(bidding.getId())) {
            return;
        }
        Sellerprofile seller = bidding.getOwner() == null
                ? null
                : sellerprofileRepository.findByUser_Id(bidding.getOwner().getId()).orElse(null);
        if (seller == null) {
            log.warn("Bidding {} closed without a seller profile; no payment created", bidding.getId());
            return;
        }

        Date now = new Date();
        Payment payment = new Payment();
        payment.setBidding(bidding);
        payment.setBuyer(bidding.getWinner());
        payment.setSellerprofile(seller);
        payment.setAmount(bidding.getLastBid());
        payment.setCreatedAt(now);
        payment.setDueDate(new Date(now.getTime() + TimeUnit.DAYS.toMillis(dueDays)));
        paymentRepository.save(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Payment getPayment(Long paymentID, Long userID) {
        Payment payment = paymentRepository.findById(paymentID)
                .orElseThrow(() -> notFound(paymentID));
        if (!isBuyer(payment, userID) && !isSeller(payment, userID)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only the buyer or the seller can view this payment");
        }
        return payment;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Payment> getPurchases(Long buyerID, Payment.Status status, Pageable pageable) {
        validateSort(pageable);
        return status == null
                ? paymentRepository.findByBuyer_Id(buyerID, pageable)
                : paymentRepository.findByBuyer_IdAndStatus(buyerID, status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> getPurchasesForBiddings(Long buyerID, Collection<Long> biddingIDs) {
        return biddingIDs.isEmpty()
                ? List.of()
                : paymentRepository.findByBuyer_IdAndBidding_IdIn(buyerID, biddingIDs);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Payment> findPaymentForParticipant(Long biddingID, Long userID) {
        return paymentRepository.findByBidding_Id(biddingID)
                .filter(payment -> isBuyer(payment, userID) || isSeller(payment, userID));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Payment> getSales(Long sellerUserID, Payment.Status status, Pageable pageable) {
        validateSort(pageable);
        return status == null
                ? paymentRepository.findBySellerprofile_User_Id(sellerUserID, pageable)
                : paymentRepository.findBySellerprofile_User_IdAndStatus(sellerUserID, status, pageable);
    }

    @Override
    @Transactional
    public Payment submitSlip(Long paymentID, Long buyerID, String slipUrl) {
        Payment payment = findForUpdate(paymentID);
        if (!isBuyer(payment, buyerID)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the buyer can pay for this payment");
        }
        ensureCanMove(payment, Payment.Status.PAYMENT_SUBMITTED);
        if (new Date().after(payment.getDueDate())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The payment deadline has passed");
        }
        String address = payment.getBuyer().getAddress();
        if (address == null || address.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Add a shipping address to your profile before paying");
        }

        payment.setSlipUrl(slipUrl.trim());
        payment.setPaidAt(new Date());
        payment.setShippingAddress(address.trim());
        payment.setRejectReason(null);
        payment.setStatus(Payment.Status.PAYMENT_SUBMITTED);
        return paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public Payment confirmPayment(Long paymentID, Long sellerUserID) {
        Payment payment = findSellersPaymentForUpdate(paymentID, sellerUserID);
        ensureCanMove(payment, Payment.Status.PAID);

        payment.setConfirmedAt(new Date());
        payment.setStatus(Payment.Status.PAID);
        return paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public Payment rejectPayment(Long paymentID, Long sellerUserID, String reason) {
        Payment payment = findSellersPaymentForUpdate(paymentID, sellerUserID);
        ensureCanMove(payment, Payment.Status.AWAITING_PAYMENT);

        Date earliestDue = new Date(System.currentTimeMillis() + RESUBMIT_GRACE_MS);
        if (payment.getDueDate().before(earliestDue)) {
            payment.setDueDate(earliestDue);
        }
        payment.setRejectReason(reason.trim());
        payment.setSlipUrl(null);
        payment.setPaidAt(null);
        payment.setStatus(Payment.Status.AWAITING_PAYMENT);
        return paymentRepository.save(payment);
    }

    // Sending the shipping details completes the sale; the buyer does not confirm delivery.
    @Override
    @Transactional
    public Payment ship(Long paymentID, Long sellerUserID, String carrier, String trackingNumber,
            String trackingUrl, String shippingNote) {
        Payment payment = findSellersPaymentForUpdate(paymentID, sellerUserID);
        ensureCanMove(payment, Payment.Status.COMPLETED);

        Date now = new Date();
        payment.setCarrier(carrier.trim());
        payment.setTrackingNumber(trackingNumber.trim());
        payment.setTrackingUrl(trackingUrl.trim());
        payment.setShippingNote(shippingNote == null || shippingNote.isBlank() ? null : shippingNote.trim());
        payment.setShippedAt(now);
        payment.setCompletedAt(now);
        payment.setStatus(Payment.Status.COMPLETED);
        Payment saved = paymentRepository.save(payment);

        sellerprofileRepository.addToSalecount(saved.getSellerprofile().getSellprofileId(), 1);
        return saved;
    }

    @Override
    @Transactional
    public int expireOverduePayments() {
        List<Payment> overdue = paymentRepository
                .findByStatusAndDueDateBefore(Payment.Status.AWAITING_PAYMENT, new Date());
        overdue.forEach(payment -> payment.setStatus(Payment.Status.EXPIRED));
        paymentRepository.saveAll(overdue);
        return overdue.size();
    }

    private Payment findForUpdate(Long paymentID) {
        return paymentRepository.findByIdForUpdate(paymentID)
                .orElseThrow(() -> notFound(paymentID));
    }

    private Payment findSellersPaymentForUpdate(Long paymentID, Long sellerUserID) {
        Payment payment = findForUpdate(paymentID);
        if (!isSeller(payment, sellerUserID)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the seller can update this payment");
        }
        return payment;
    }

    private void ensureCanMove(Payment payment, Payment.Status target) {
        if (!stateResolver.resolve(payment.getStatus()).canMoveTo(target)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot change payment from " + payment.getStatus() + " to " + target);
        }
    }

    private boolean isBuyer(Payment payment, Long userID) {
        return userID != null && payment.getBuyer().getId().equals(userID);
    }

    private boolean isSeller(Payment payment, Long userID) {
        return userID != null && payment.getSellerprofile().getUser().getId().equals(userID);
    }

    private ResponseStatusException notFound(Long paymentID) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found: " + paymentID);
    }

    static void validateSort(Pageable pageable) {
        for (Sort.Order order : pageable.getSort()) {
            if (!SORTABLE_FIELDS.contains(order.getProperty())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Cannot sort by: " + order.getProperty());
            }
        }
    }
}
