package com.rupeek.maidbooking.service;

import com.rupeek.maidbooking.model.booking.Booking;
import com.rupeek.maidbooking.model.booking.BookingStatus;
import com.rupeek.maidbooking.model.maid.Maid;
import com.rupeek.maidbooking.model.maid.TimeSlot;
import com.rupeek.maidbooking.repository.BookingRepository;
import com.rupeek.maidbooking.repository.MaidRepository;
import com.rupeek.maidbooking.strategy.filter.MaidFilter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MaidService {
    private final MaidRepository maidRepository;
    private final BookingRepository bookingRepository;

    public MaidService(MaidRepository maidRepository, BookingRepository bookingRepository) {
        this.maidRepository = maidRepository;
        this.bookingRepository = bookingRepository;
    }

    public Maid onboardMaid(Maid maid) {
        if (maid.getName() == null || maid.getName().isBlank()) {
            throw new IllegalArgumentException("Maid name is required");
        }
        if (maid.getLocality() == null || maid.getLocality().isBlank()) {
            throw new IllegalArgumentException("Maid locality is required");
        }
        if (maid.getHourlyRate() <= 0) {
            throw new IllegalArgumentException("Hourly rate must be positive");
        }
        if (maid.getSkills() == null || maid.getSkills().isEmpty()) {
            throw new IllegalArgumentException("At least one skill is required");
        }
        return maidRepository.save(maid);
    }

    public Maid getMaid(String maidId) {
        return maidRepository.findById(maidId)
                .orElseThrow(() -> new IllegalArgumentException("Maid not found: " + maidId));
    }

    public boolean isMaidAvailable(Maid maid, TimeSlot requestedSlot) {
        if (!maid.isWorkingDuring(requestedSlot)) {
            return false;
        }
        List<Booking> existingBookings = bookingRepository.findByMaidId(maid.getId());
        for (Booking booking : existingBookings) {
            if (booking.getStatus() != BookingStatus.CANCELLED) {
                for (TimeSlot bookedSlot : booking.getSlots()) {
                    if (bookedSlot.overlaps(requestedSlot)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public List<Maid> searchMaids(TimeSlot requiredSlot, List<MaidFilter> filters) {
        List<Maid> availableMaids = new ArrayList<>();
        for (Maid maid : maidRepository.findAll()) {
            if (requiredSlot != null && !isMaidAvailable(maid, requiredSlot)) {
                continue;
            }
            if (matchesAllFilters(maid, filters)) {
                availableMaids.add(maid);
            }
        }
        return availableMaids;
    }

    private boolean matchesAllFilters(Maid maid, List<MaidFilter> filters) {
        if (filters == null) {
            return true;
        }
        for (MaidFilter filter : filters) {
            if (!filter.matches(maid)) {
                return false;
            }
        }
        return true;
    }
}
