package com.example.project.service.state;

import com.example.project.model.Payment;

public interface PaymentState {
    Payment.Status getStatus();

    boolean canMoveTo(Payment.Status target);
}
