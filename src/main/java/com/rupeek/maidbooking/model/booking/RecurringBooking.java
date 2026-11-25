package com.rupeek.maidbooking.model.booking;

import com.rupeek.maidbooking.model.maid.ServiceType;
import com.rupeek.maidbooking.model.maid.TimeSlot;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RecurringBooking extends Booking {
    private final List<TimeSlot> occurrences = new ArrayList<>();
    private final Set<TimeSlot> paidOccurrences = new HashSet<>();

    public RecurringBooking(String userId, String maidId, ServiceType serviceType,
                            LocalDateTime startTime, int durationHours,
                            Set<DayOfWeek> recurringDays, int occurrencesCount) {
        super(userId, maidId, serviceType);
        if (startTime == null || startTime.isBefore(LocalDateTime.now()) || durationHours <= 0) {
            throw new IllegalArgumentException("Recurring booking requires a future startTime and positive durationHours");
        }
        if (recurringDays == null || recurringDays.isEmpty() || occurrencesCount <= 0) {
            throw new IllegalArgumentException("Recurring booking requires recurringDays and occurrencesCount > 0");
        }

        LocalDateTime cursor = startTime;
        while (occurrences.size() < occurrencesCount) {
            if (recurringDays.contains(cursor.getDayOfWeek())) {
                occurrences.add(new TimeSlot(cursor, cursor.plusHours(durationHours)));
            }
            cursor = cursor.plusDays(1);
        }
    }

    @Override
    public BookingType getBookingType() {
        return BookingType.RECURRING;
    }

    @Override
    public List<TimeSlot> getSlots() {
        return occurrences;
    }

    public TimeSlot getOccurrence(int index) {
        if (index < 0 || index >= occurrences.size()) {
            throw new IllegalArgumentException("Invalid occurrence index: " + index);
        }
        return occurrences.get(index);
    }

    public boolean isOccurrencePaid(TimeSlot slot) {
        return paidOccurrences.contains(slot);
    }

    public void markOccurrencePaid(TimeSlot slot) {
        paidOccurrences.add(slot);
    }

    public void markAllOccurrencesPaid() {
        paidOccurrences.addAll(occurrences);
    }

    public List<TimeSlot> getUnpaidOccurrences() {
        List<TimeSlot> unpaid = new ArrayList<>();
        for (TimeSlot slot : occurrences) {
            if (!paidOccurrences.contains(slot)) {
                unpaid.add(slot);
            }
        }
        return unpaid;
    }

    public Set<TimeSlot> getPaidOccurrences() {
        return paidOccurrences;
    }

    public TimeSlot removeOccurrence(int index) {
        TimeSlot removed = getOccurrence(index);
        occurrences.remove(index);
        return removed;
    }
}
