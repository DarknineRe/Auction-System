package com.example.project.service.gateway;

import java.util.Date;

// A charge as the payment provider reports it. Amounts are in minor units (satang).
public record GatewayCharge(
        String id,
        Status status,
        long amountMinor,
        String currency,
        Long paymentId,
        String qrImageUrl,
        Date expiresAt) {

    public enum Status {
        PENDING,
        SUCCESSFUL,
        FAILED
    }
}
