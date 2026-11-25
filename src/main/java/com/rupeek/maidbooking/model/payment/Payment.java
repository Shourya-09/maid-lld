package com.rupeek.maidbooking.model.payment;

import java.time.LocalDateTime;
import java.util.UUID;

public class Payment {
    private String id;
    private String bookingId;
    private double amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private LocalDateTime timestamp;

    public Payment() {
    }

    public Payment(String bookingId, double amount, PaymentMethod paymentMethod, PaymentStatus status) {
        this.id = UUID.randomUUID().toString();
        this.bookingId = bookingId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getBookingId() {
        return bookingId;
    }

    public double getAmount() {
        return amount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
