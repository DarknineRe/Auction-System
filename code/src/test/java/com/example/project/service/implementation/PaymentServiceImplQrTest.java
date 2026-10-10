package com.example.project.service.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.example.project.model.Payment;
import com.example.project.model.Sellerprofile;
import com.example.project.model.User;
import com.example.project.repository.PaymentRepository;
import com.example.project.repository.SellerprofileRepository;
import com.example.project.service.gateway.GatewayCharge;
import com.example.project.service.gateway.PaymentGateway;
import com.example.project.service.state.AwaitingPaymentState;
import com.example.project.service.state.CancelledPaymentState;
import com.example.project.service.state.CompletedPaymentState;
import com.example.project.service.state.ExpiredPaymentState;
import com.example.project.service.state.PaidPaymentState;
import com.example.project.service.state.PaymentStateResolver;
import com.example.project.service.state.PaymentSubmittedState;

class PaymentServiceImplQrTest {

    private static final Long PAYMENT_ID = 12L;
    private static final Long BUYER_ID = 1L;
    private static final Long SELLER_ID = 2L;
    private static final String CHARGE_ID = "chrg_test_123";

    private PaymentRepository paymentRepository;
    private PaymentGateway gateway;
    private PaymentServiceImpl service;
    private Payment payment;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        gateway = mock(PaymentGateway.class);
        when(gateway.isEnabled()).thenReturn(true);
        PaymentStateResolver stateResolver = new PaymentStateResolver(List.of(
                new AwaitingPaymentState(), new PaymentSubmittedState(), new PaidPaymentState(),
                new CompletedPaymentState(), new ExpiredPaymentState(), new CancelledPaymentState()));
        service = new PaymentServiceImpl(paymentRepository, mock(SellerprofileRepository.class),
                stateResolver, mock(PaymentExpiryProcessor.class), gateway, 3);

        User buyer = new User();
        buyer.setId(BUYER_ID);
        buyer.setAddress(" 1 Test Road ");
        User seller = new User();
        seller.setId(SELLER_ID);
        Sellerprofile sellerprofile = new Sellerprofile();
        sellerprofile.setUser(seller);

        payment = new Payment();
        payment.setId(PAYMENT_ID);
        payment.setBuyer(buyer);
        payment.setSellerprofile(sellerprofile);
        payment.setAmount(new BigDecimal("1500.5000"));
        payment.setStatus(Payment.Status.AWAITING_PAYMENT);
        payment.setDueDate(new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(3)));
        when(paymentRepository.findByIdForUpdate(PAYMENT_ID)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(call -> call.getArgument(0));
    }

    @Test
    void startQrPaymentStoresChargeAndReusesItWhileValid() {
        Date expiresAt = new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1));
        when(gateway.createPromptPayCharge(PAYMENT_ID, payment.getAmount()))
                .thenReturn(charge(GatewayCharge.Status.PENDING, 150050, "https://qr.test/code.svg", expiresAt));

        service.startQrPayment(PAYMENT_ID, BUYER_ID);
        service.startQrPayment(PAYMENT_ID, BUYER_ID);

        assertEquals(CHARGE_ID, payment.getGatewayChargeId());
        assertEquals("https://qr.test/code.svg", payment.getQrImageUrl());
        assertEquals(expiresAt, payment.getQrExpiresAt());
        assertEquals(Payment.Status.AWAITING_PAYMENT, payment.getStatus());
        verify(gateway).createPromptPayCharge(PAYMENT_ID, payment.getAmount());
    }

    @Test
    void startQrPaymentIsOnlyForTheBuyer() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.startQrPayment(PAYMENT_ID, SELLER_ID));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(gateway, never()).createPromptPayCharge(any(), any());
    }

    @Test
    void amountBelowProviderMinimumFallsBackToSlip() {
        payment.setAmount(new BigDecimal("12.0000"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.startQrPayment(PAYMENT_ID, BUYER_ID));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertFalse(service.isQrPaymentAvailable(payment));
        verify(gateway, never()).createPromptPayCharge(any(), any());
    }

    @Test
    void successfulChargeMarksPaymentPaidOnce() {
        when(gateway.getCharge(CHARGE_ID))
                .thenReturn(charge(GatewayCharge.Status.SUCCESSFUL, 150050, null, null));

        service.handleGatewayCharge(CHARGE_ID);
        Date paidAt = payment.getPaidAt();
        service.handleGatewayCharge(CHARGE_ID);

        assertEquals(Payment.Status.PAID, payment.getStatus());
        assertEquals("1 Test Road", payment.getShippingAddress());
        assertNotNull(payment.getConfirmedAt());
        assertEquals(paidAt, payment.getPaidAt());
        verify(paymentRepository).save(payment);
    }

    @Test
    void pendingChargeLeavesPaymentUnpaid() {
        payment.setGatewayChargeId(CHARGE_ID);
        when(gateway.getCharge(CHARGE_ID))
                .thenReturn(charge(GatewayCharge.Status.PENDING, 150050, null, null));

        service.refreshQrPayment(PAYMENT_ID, BUYER_ID);

        assertEquals(Payment.Status.AWAITING_PAYMENT, payment.getStatus());
        assertNull(payment.getPaidAt());
    }

    @Test
    void chargeWithWrongAmountIsNotAccepted() {
        when(gateway.getCharge(CHARGE_ID))
                .thenReturn(charge(GatewayCharge.Status.SUCCESSFUL, 100, null, null));

        service.handleGatewayCharge(CHARGE_ID);

        assertEquals(Payment.Status.AWAITING_PAYMENT, payment.getStatus());
    }

    @Test
    void paidChargeDoesNotReviveExpiredPayment() {
        payment.setStatus(Payment.Status.EXPIRED);
        when(gateway.getCharge(CHARGE_ID))
                .thenReturn(charge(GatewayCharge.Status.SUCCESSFUL, 150050, null, null));

        service.handleGatewayCharge(CHARGE_ID);

        assertEquals(Payment.Status.EXPIRED, payment.getStatus());
    }

    @Test
    void sellerCannotConfirmPaymentWithoutSlip() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.confirmPayment(PAYMENT_ID, SELLER_ID));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals(Payment.Status.AWAITING_PAYMENT, payment.getStatus());
    }

    private GatewayCharge charge(GatewayCharge.Status status, long amountMinor, String qrImageUrl, Date expiresAt) {
        return new GatewayCharge(CHARGE_ID, status, amountMinor, PaymentGateway.CURRENCY, PAYMENT_ID,
                qrImageUrl, expiresAt);
    }
}
