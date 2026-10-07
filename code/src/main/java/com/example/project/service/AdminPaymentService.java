package com.example.project.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.project.model.Payment;

public interface AdminPaymentService {
    Page<Payment> getPayments(Payment.Status status, Pageable pageable);

    Payment getPayment(Long paymentId);

    Payment cancelPayment(Long paymentId, String reason);
}
