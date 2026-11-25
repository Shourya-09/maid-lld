package com.rupeek.maidbooking.repository;

import com.rupeek.maidbooking.model.payment.Payment;

import java.util.List;

public interface PaymentRepository {
    Payment save(Payment payment);
    List<Payment> findByBookingId(String bookingId);
}
