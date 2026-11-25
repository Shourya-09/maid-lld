package com.rupeek.maidbooking.strategy.payment;

import com.rupeek.maidbooking.model.payment.PaymentMethod;
import com.rupeek.maidbooking.model.user.User;

public class UpiPaymentStrategy implements PaymentMethod {
    private final User user;

    public UpiPaymentStrategy(User user) {
        this.user = user;
    }

    public String getUpiId() {
        return user != null ? user.getUpiId() : null;
    }

    @Override
    public boolean pay(double amount) {
        String upiId = getUpiId();
        if (upiId == null || upiId.isBlank()) {
            throw new IllegalArgumentException("User UPI ID is missing");
        }
        return amount > 0;
    }
}
