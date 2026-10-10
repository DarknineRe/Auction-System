package com.example.project.service.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.example.project.model.Payment;
import com.example.project.repository.PaymentRepository;

class PaymentExpiryProcessorTest {

    @Test
    void expiresOnlyStillAwaitingOverduePayment() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        Payment payment = new Payment();
        payment.setStatus(Payment.Status.AWAITING_PAYMENT);
        payment.setDueDate(new Date(1_000));
        when(paymentRepository.findByIdForUpdate(12L)).thenReturn(Optional.of(payment));
        PaymentExpiryProcessor processor = new PaymentExpiryProcessor(paymentRepository);

        assertTrue(processor.expireOverduePayment(12L, new Date(2_000)));
        assertEquals(Payment.Status.EXPIRED, payment.getStatus());
    }

    @Test
    void skipsPaymentWhenStatusChangedBeforeLockWasAcquired() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        Payment payment = new Payment();
        payment.setStatus(Payment.Status.PAYMENT_SUBMITTED);
        payment.setDueDate(new Date(1_000));
        when(paymentRepository.findByIdForUpdate(12L)).thenReturn(Optional.of(payment));
        PaymentExpiryProcessor processor = new PaymentExpiryProcessor(paymentRepository);

        assertFalse(processor.expireOverduePayment(12L, new Date(2_000)));
        assertEquals(Payment.Status.PAYMENT_SUBMITTED, payment.getStatus());
    }
}
