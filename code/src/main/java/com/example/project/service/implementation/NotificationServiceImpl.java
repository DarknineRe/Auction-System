package com.example.project.service.implementation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.project.dto.response.NotificationResponse;
import com.example.project.model.Payment;
import com.example.project.service.NotificationService;
import com.example.project.service.PaymentService;

// Notifications are derived from payment status, so there is nothing to store or mark as read:
// an item disappears as soon as the user does what it asks.
@Service
public class NotificationServiceImpl implements NotificationService {

    private static final int MAX_PER_KIND = 5;
    private static final Pageable MOST_URGENT =
            PageRequest.of(0, MAX_PER_KIND, Sort.by(Sort.Direction.ASC, "dueDate"));

    private final PaymentService paymentService;

    public NotificationServiceImpl(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(Long userID) {
        List<NotificationResponse> notifications = new ArrayList<>();
        paymentService.getPurchases(userID, Payment.Status.AWAITING_PAYMENT, MOST_URGENT)
                .forEach(payment -> notifications.add(toNotification(payment,
                        payment.getRejectReason() == null
                                ? NotificationResponse.Type.PAYMENT_DUE
                                : NotificationResponse.Type.SLIP_REJECTED)));
        paymentService.getSales(userID, Payment.Status.PAYMENT_SUBMITTED, MOST_URGENT)
                .forEach(payment -> notifications.add(
                        toNotification(payment, NotificationResponse.Type.SLIP_TO_REVIEW)));
        paymentService.getSales(userID, Payment.Status.PAID, MOST_URGENT)
                .forEach(payment -> notifications.add(
                        toNotification(payment, NotificationResponse.Type.READY_TO_SHIP)));
        return notifications;
    }

    private NotificationResponse toNotification(Payment payment, NotificationResponse.Type type) {
        return new NotificationResponse(
                type,
                payment.getId(),
                payment.getBidding().getId(),
                payment.getAmount(),
                payment.getDueDate());
    }
}
