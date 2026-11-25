package com.rupeek.maidbooking.service;

import com.rupeek.maidbooking.model.booking.Booking;
import com.rupeek.maidbooking.model.booking.BookingStatus;
import com.rupeek.maidbooking.model.booking.RecurringBooking;
import com.rupeek.maidbooking.model.maid.Maid;
import com.rupeek.maidbooking.model.maid.TimeSlot;
import com.rupeek.maidbooking.model.payment.Payment;
import com.rupeek.maidbooking.model.payment.PaymentStatus;
import com.rupeek.maidbooking.repository.BookingRepository;
import com.rupeek.maidbooking.repository.PaymentRepository;
import com.rupeek.maidbooking.repository.UserRepository;
import com.rupeek.maidbooking.model.payment.RefundPolicy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class BookingService {
    private final MaidService maidService;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final RefundPolicy refundPolicy;
    private final ConcurrentHashMap<String, ReentrantLock> maidLocks = new ConcurrentHashMap<>();

    public BookingService(MaidService maidService,
                          BookingRepository bookingRepository,
                          UserRepository userRepository,
                          PaymentRepository paymentRepository,
                          RefundPolicy refundPolicy) {
        this.maidService = maidService;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
        this.refundPolicy = refundPolicy;
    }

    public Booking createBooking(Booking booking) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking cannot be null");
        }
        if (booking.getUserId() == null || booking.getUserId().isBlank()) {
            throw new IllegalArgumentException("User ID is required");
        }
        userRepository.findById(booking.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + booking.getUserId()));

        Maid maid = maidService.getMaid(booking.getMaidId());
        if (booking.getServiceType() == null || !maid.getSkills().contains(booking.getServiceType())) {
            throw new IllegalArgumentException("Maid does not offer requested service: " + booking.getServiceType());
        }

        List<TimeSlot> slots = booking.getSlots();
        ReentrantLock lock = maidLocks.computeIfAbsent(maid.getId(), id -> new ReentrantLock());
        lock.lock();
        try {
            for (TimeSlot slot : slots) {
                if (!maidService.isMaidAvailable(maid, slot)) {
                    throw new IllegalStateException("Maid is not available for slot: "
                            + slot.getStart() + " to " + slot.getEnd());
                }
            }
            booking.calculateAmount(maid.getHourlyRate());
            return bookingRepository.save(booking);
        } finally {
            lock.unlock();
        }
    }

    public Booking getBooking(String bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));
    }

    public double cancelBooking(String bookingId) {
        Booking booking = getBooking(bookingId);
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking is already cancelled");
        }

        Maid maid = maidService.getMaid(booking.getMaidId());
        double refund = 0.0;

        if (booking instanceof RecurringBooking recurringBooking) {
            for (TimeSlot paidSlot : recurringBooking.getPaidOccurrences()) {
                double slotAmount = paidSlot.durationInHours() * maid.getHourlyRate();
                refund += refundPolicy.calculateRefund(slotAmount, paidSlot.getStart());
            }
            recurringBooking.getPaidOccurrences().clear();
        } else if (booking.getStatus() == BookingStatus.CONFIRMED) {
            for (TimeSlot slot : booking.getSlots()) {
                double slotAmount = slot.durationInHours() * maid.getHourlyRate();
                refund += refundPolicy.calculateRefund(slotAmount, slot.getStart());
            }
        }

        if (refund > 0) {
            markPaymentsRefunded(bookingId);
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        return refund;
    }

    public double cancelRecurringOccurrence(String bookingId, int occurrenceIndex) {
        Booking booking = getBooking(bookingId);
        if (!(booking instanceof RecurringBooking recurringBooking)) {
            throw new IllegalArgumentException("Single-occurrence cancellation is only supported for RecurringBooking");
        }
        if (recurringBooking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking is already cancelled");
        }

        Maid maid = maidService.getMaid(recurringBooking.getMaidId());
        TimeSlot removedSlot = recurringBooking.removeOccurrence(occurrenceIndex);
        double slotAmount = removedSlot.durationInHours() * maid.getHourlyRate();

        double refund = 0.0;
        if (recurringBooking.getPaidOccurrences().remove(removedSlot)) {
            refund = refundPolicy.calculateRefund(slotAmount, removedSlot.getStart());
            if (refund > 0) {
                markPaymentsRefunded(bookingId);
            }
        }

        recurringBooking.calculateAmount(maid.getHourlyRate());
        if (recurringBooking.getSlots().isEmpty()) {
            recurringBooking.setStatus(BookingStatus.CANCELLED);
        }
        bookingRepository.save(recurringBooking);
        return refund;
    }

    private void markPaymentsRefunded(String bookingId) {
        for (Payment payment : paymentRepository.findByBookingId(bookingId)) {
            if (payment.getStatus() == PaymentStatus.SUCCESS) {
                payment.setStatus(PaymentStatus.REFUNDED);
                paymentRepository.save(payment);
            }
        }
    }
}
