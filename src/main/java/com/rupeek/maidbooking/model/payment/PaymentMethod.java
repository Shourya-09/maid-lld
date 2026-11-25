package com.rupeek.maidbooking.model.payment;

public interface PaymentMethod {
    boolean pay(double amount);
}
