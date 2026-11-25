package com.rupeek.maidbooking.repository;

import com.rupeek.maidbooking.model.payment.Payment;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class InMemoryPaymentRepository implements PaymentRepository {
    private final Map<String, Payment> store = new ConcurrentHashMap<>();

    @Override
    public Payment save(Payment payment) {
        store.put(payment.getId(), payment);
        return payment;
    }

    @Override
    public List<Payment> findByBookingId(String bookingId) {
        return store.values().stream()
                .filter(p -> p.getBookingId().equals(bookingId))
                .collect(Collectors.toList());
    }
}
