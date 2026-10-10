package com.example.project.service.implementation;

import java.util.Date;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.project.model.Payment;
import com.example.project.repository.PaymentRepository;

@Service
public class PaymentExpiryProcessor {

    private final PaymentRepository paymentRepository;

    public PaymentExpiryProcessor(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean expireOverduePayment(Long paymentId, Date now) {
        Payment payment = paymentRepository.findByIdForUpdate(paymentId).orElse(null);
        if (payment == null || payment.getStatus() != Payment.Status.AWAITING_PAYMENT
                || payment.getDueDate() == null || !payment.getDueDate().before(now)) {
            return false;
        }
        payment.setStatus(Payment.Status.EXPIRED);
        return true;
    }
}
