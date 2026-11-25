package com.rupeek.maidbooking.model.booking;

import com.rupeek.maidbooking.model.maid.ServiceType;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Set;

public class BookingRequest {
    private String userId;
    private String maidId;
    private ServiceType serviceType;
    private BookingType bookingType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int durationHours;
    private Set<DayOfWeek> recurringDays;
    private int occurrencesCount;

    public BookingRequest() {
    }

    public Booking toBooking() {
        if (bookingType == null) {
            throw new IllegalArgumentException("Booking type is required");
        }
        return switch (bookingType) {
            case INSTANT -> new InstantBooking(userId, maidId, serviceType, durationHours);
            case SCHEDULED -> new ScheduledBooking(userId, maidId, serviceType, startTime, endTime);
            case RECURRING -> new RecurringBooking(userId, maidId, serviceType, startTime, durationHours, recurringDays, occurrencesCount);
        };
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

    public BookingType getBookingType() {
        return bookingType;
    }

    public void setBookingType(BookingType bookingType) {
        this.bookingType = bookingType;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public int getDurationHours() {
        return durationHours;
    }

    public void setDurationHours(int durationHours) {
        this.durationHours = durationHours;
    }

    public Set<DayOfWeek> getRecurringDays() {
        return recurringDays;
    }

    public void setRecurringDays(Set<DayOfWeek> recurringDays) {
        this.recurringDays = recurringDays;
    }

    public int getOccurrencesCount() {
        return occurrencesCount;
    }

    public void setOccurrencesCount(int occurrencesCount) {
        this.occurrencesCount = occurrencesCount;
    }
}
