package com.example.project.service.state;

import org.springframework.stereotype.Component;

import com.example.project.model.Payment;

@Component
public class AwaitingPaymentState implements PaymentState {

    @Override
    public Payment.Status getStatus() {
        return Payment.Status.AWAITING_PAYMENT;
    }

    // Straight to PAID only happens when the payment gateway confirms a QR payment.
    @Override
    public boolean canMoveTo(Payment.Status target) {
        return target == Payment.Status.PAYMENT_SUBMITTED || target == Payment.Status.PAID
                || target == Payment.Status.EXPIRED || target == Payment.Status.CANCELLED;
    }
}
