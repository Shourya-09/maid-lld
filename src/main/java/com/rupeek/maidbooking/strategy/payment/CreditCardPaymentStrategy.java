package com.rupeek.maidbooking.strategy.payment;

import com.rupeek.maidbooking.model.payment.PaymentMethod;
import com.rupeek.maidbooking.model.user.User;

public class CreditCardPaymentStrategy implements PaymentMethod {
    private final User user;

    public CreditCardPaymentStrategy(User user) {
        this.user = user;
    }

    public String getCreditCardNumber() {
        return user != null ? user.getCreditCardNumber() : null;
    }

    @Override
    public boolean pay(double amount) {
        String ccNumber = getCreditCardNumber();
        if (ccNumber == null || ccNumber.isBlank()) {
            throw new IllegalArgumentException("User Credit Card number is missing");
        }
        return amount > 0;
    }
}
