package com.example.project.dto.response;

import java.math.BigDecimal;
import java.util.Date;

// One actionable item shown in the navbar bell; every item links to its payment page.
public record NotificationResponse(
        Type type,
        Long paymentId,
        Long biddingId,
        BigDecimal amount,
        Date dueDate) {

    public enum Type {
        PAYMENT_DUE,
        SLIP_REJECTED,
        SLIP_TO_REVIEW,
        READY_TO_SHIP
    }
}
