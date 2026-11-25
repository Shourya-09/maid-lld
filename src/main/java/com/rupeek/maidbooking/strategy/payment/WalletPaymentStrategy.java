package com.rupeek.maidbooking.strategy.payment;

import com.rupeek.maidbooking.model.payment.PaymentMethod;
import com.rupeek.maidbooking.model.user.User;

public class WalletPaymentStrategy implements PaymentMethod {
    private final User user;

    public WalletPaymentStrategy(User user) {
        this.user = user;
    }

    public String getWalletId() {
        return user != null ? user.getWalletId() : null;
    }

    @Override
    public boolean pay(double amount) {
        String walletId = getWalletId();
        if (walletId == null || walletId.isBlank()) {
            throw new IllegalArgumentException("User Wallet ID is missing");
        }
        return amount > 0;
    }
}
