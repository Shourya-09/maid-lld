package com.rupeek.maidbooking.model.maid;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class TimeSlot {
    private LocalDateTime start;
    private LocalDateTime end;

    public TimeSlot() {
    }

    public TimeSlot(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || !end.isAfter(start)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        this.start = start;
        this.end = end;
    }

    public boolean overlaps(TimeSlot other) {
        return this.start.isBefore(other.end) && other.start.isBefore(this.end);
    }

    public double durationInHours() {
        return Duration.between(start, end).toMinutes() / 60.0;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeSlot timeSlot)) return false;
        return Objects.equals(start, timeSlot.start) && Objects.equals(end, timeSlot.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }
}
