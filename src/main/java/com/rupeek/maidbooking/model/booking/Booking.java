package com.rupeek.maidbooking.model.booking;

import com.rupeek.maidbooking.model.maid.ServiceType;
import com.rupeek.maidbooking.model.maid.TimeSlot;

import java.util.List;
import java.util.UUID;

public abstract class Booking {
    protected String id;
    protected String userId;
    protected String maidId;
    protected ServiceType serviceType;
    protected double amount;
    protected BookingStatus status;

    protected Booking() {
        this.id = UUID.randomUUID().toString();
        this.status = BookingStatus.PENDING_PAYMENT;
    }

    protected Booking(String userId, String maidId, ServiceType serviceType) {
        this();
        this.userId = userId;
        this.maidId = maidId;
        this.serviceType = serviceType;
    }

    public abstract BookingType getBookingType();

    public abstract List<TimeSlot> getSlots();

    public void calculateAmount(double maidHourlyRate) {
        this.amount = getSlots().stream()
                .mapToDouble(slot -> slot.durationInHours() * maidHourlyRate)
                .sum();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMaidId() {
        return maidId;
    }

    public void setMaidId(String maidId) {
        this.maidId = maidId;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }
}
