package com.example.project.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.project.service.PaymentService;

@Component
public class PaymentExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(PaymentExpiryScheduler.class);

    private final PaymentService paymentService;

    public PaymentExpiryScheduler(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Scheduled(fixedDelayString = "${app.payment.expiry-check-ms:60000}")
    public void expireOverduePayments() {
        int expired = paymentService.expireOverduePayments();
        if (expired > 0) {
            log.info("Expired {} overdue payment(s)", expired);
        }
    }
}
