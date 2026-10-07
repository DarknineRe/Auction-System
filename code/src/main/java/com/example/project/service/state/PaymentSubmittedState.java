package com.example.project.service.state;

import org.springframework.stereotype.Component;

import com.example.project.model.Payment;

@Component
public class PaymentSubmittedState implements PaymentState {

    @Override
    public Payment.Status getStatus() {
        return Payment.Status.PAYMENT_SUBMITTED;
    }

    @Override
    public boolean canMoveTo(Payment.Status target) {
        return target == Payment.Status.PAID || target == Payment.Status.AWAITING_PAYMENT || target == Payment.Status.CANCELLED;
    }
}
