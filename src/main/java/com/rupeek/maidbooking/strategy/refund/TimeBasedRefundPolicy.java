package com.rupeek.maidbooking.strategy.refund;

import com.rupeek.maidbooking.model.payment.RefundPolicy;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class TimeBasedRefundPolicy implements RefundPolicy {

    @Override
    public double calculateRefund(double paidAmount, LocalDateTime slotStartTime) {
        if (paidAmount <= 0) {
            return 0.0;
        }
        long hoursBeforeSlot = Duration.between(LocalDateTime.now(), slotStartTime).toHours();
        if (hoursBeforeSlot >= 24) {
            return paidAmount;
        } else if (hoursBeforeSlot >= 2) {
            return paidAmount * 0.5;
        }
        return 0.0;
    }
}
