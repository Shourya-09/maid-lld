package com.rupeek.maidbooking.model.maid;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class AvailabilityWindow {
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;

    public AvailabilityWindow() {
    }

    public AvailabilityWindow(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        if (dayOfWeek == null || startTime == null || endTime == null || !endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("Invalid availability window");
        }
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public boolean covers(TimeSlot slot) {
        if (!slot.getStart().toLocalDate().equals(slot.getEnd().toLocalDate())) {
            return false;
        }
        if (slot.getStart().getDayOfWeek() != dayOfWeek) {
            return false;
        }
        LocalTime slotStart = slot.getStart().toLocalTime();
        LocalTime slotEnd = slot.getEnd().toLocalTime();
        return !slotStart.isBefore(startTime) && !slotEnd.isAfter(endTime);
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }
}
