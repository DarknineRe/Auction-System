package com.example.project.service.state;

import org.springframework.stereotype.Component;

import com.example.project.model.Payment;

@Component
public class PaidPaymentState implements PaymentState {

    @Override
    public Payment.Status getStatus() {
        return Payment.Status.PAID;
    }

    @Override
    public boolean canMoveTo(Payment.Status target) {
        return target == Payment.Status.COMPLETED || target == Payment.Status.CANCELLED;
    }
}
