package com.example.project.service.state;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.example.project.model.Payment;

@Component
public class PaymentStateResolver {

    private final Map<Payment.Status, PaymentState> states = new EnumMap<>(Payment.Status.class);

    public PaymentStateResolver(List<PaymentState> stateList) {
        for (PaymentState state : stateList) {
            states.put(state.getStatus(), state);
        }
    }

    public PaymentState resolve(Payment.Status status) {
        PaymentState state = states.get(status);
        if (state == null) {
            throw new IllegalStateException("No state registered for " + status);
        }
        return state;
    }
}
