package com.example.project.service.gateway;

import java.math.BigDecimal;
import java.math.RoundingMode;

public interface PaymentGateway {
    String CURRENCY = "THB";
    // The provider rejects PromptPay charges below this; smaller payments are paid by bank slip.
    BigDecimal MIN_AMOUNT = new BigDecimal("20");

    // False when no provider credentials are configured; payments then fall back to bank slips.
    boolean isEnabled();

    GatewayCharge createPromptPayCharge(Long paymentId, BigDecimal amount);

    // Always asks the provider, so the result can be trusted where a webhook body cannot.
    GatewayCharge getCharge(String chargeId);

    static long toMinorUnits(BigDecimal amount) {
        return amount.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }
}
