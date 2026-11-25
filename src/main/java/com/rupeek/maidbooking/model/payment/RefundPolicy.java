package com.rupeek.maidbooking.model.payment;

import java.time.LocalDateTime;

public interface RefundPolicy {
    double calculateRefund(double paidAmount, LocalDateTime slotStartTime);
}
