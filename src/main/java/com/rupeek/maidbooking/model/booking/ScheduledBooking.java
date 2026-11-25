package com.rupeek.maidbooking.model.booking;

import com.rupeek.maidbooking.model.maid.ServiceType;
import com.rupeek.maidbooking.model.maid.TimeSlot;

import java.time.LocalDateTime;
import java.util.List;

public class ScheduledBooking extends Booking {
    private final TimeSlot slot;

    public ScheduledBooking(String userId, String maidId, ServiceType serviceType,
                            LocalDateTime startTime, LocalDateTime endTime) {
        super(userId, maidId, serviceType);
        if (startTime == null || startTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Scheduled booking startTime must be in the future");
        }
        this.slot = new TimeSlot(startTime, endTime);
    }

    @Override
    public BookingType getBookingType() {
        return BookingType.SCHEDULED;
    }

    @Override
    public List<TimeSlot> getSlots() {
        return List.of(slot);
    }

    public TimeSlot getSlot() {
        return slot;
    }
}
