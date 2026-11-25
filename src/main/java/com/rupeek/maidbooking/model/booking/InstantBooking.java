package com.rupeek.maidbooking.model.booking;

import com.rupeek.maidbooking.model.maid.ServiceType;
import com.rupeek.maidbooking.model.maid.TimeSlot;

import java.time.LocalDateTime;
import java.util.List;

public class InstantBooking extends Booking {
    private final TimeSlot slot;

    public InstantBooking(String userId, String maidId, ServiceType serviceType, int durationHours) {
        super(userId, maidId, serviceType);
        if (durationHours <= 0) {
            throw new IllegalArgumentException("Instant booking duration must be at least 1 hour");
        }
        LocalDateTime now = LocalDateTime.now();
        this.slot = new TimeSlot(now, now.plusHours(durationHours));
    }

    @Override
    public BookingType getBookingType() {
        return BookingType.INSTANT;
    }

    @Override
    public List<TimeSlot> getSlots() {
        return List.of(slot);
    }

    public TimeSlot getSlot() {
        return slot;
    }
}
