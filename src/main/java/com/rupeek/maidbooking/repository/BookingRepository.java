package com.rupeek.maidbooking.repository;

import com.rupeek.maidbooking.model.booking.Booking;

import java.util.List;
import java.util.Optional;

public interface BookingRepository {
    Booking save(Booking booking);
    Optional<Booking> findById(String id);
    List<Booking> findByMaidId(String maidId);
}
