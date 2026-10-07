package com.example.project.service.state;

import org.springframework.stereotype.Component;

import com.example.project.model.Payment;

@Component
public class CancelledPaymentState implements PaymentState {

    @Override
    public Payment.Status getStatus() {
        return Payment.Status.CANCELLED;
    }

    @Override
    public boolean canMoveTo(Payment.Status target) {
        return false;
    }
}
