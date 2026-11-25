package com.rupeek.maidbooking.service;

import com.rupeek.maidbooking.model.booking.Booking;
import com.rupeek.maidbooking.model.booking.BookingStatus;
import com.rupeek.maidbooking.model.booking.RecurringBooking;
import com.rupeek.maidbooking.model.maid.Maid;
import com.rupeek.maidbooking.model.maid.TimeSlot;
import com.rupeek.maidbooking.model.payment.Payment;
import com.rupeek.maidbooking.model.payment.PaymentMethod;
import com.rupeek.maidbooking.model.payment.PaymentStatus;
import com.rupeek.maidbooking.repository.BookingRepository;
import com.rupeek.maidbooking.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentService {
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final MaidService maidService;

    public PaymentService(BookingRepository bookingRepository,
                          PaymentRepository paymentRepository,
                          MaidService maidService) {
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.maidService = maidService;
    }

    public Payment payForBooking(String bookingId, PaymentMethod paymentMethod) {
        if (paymentMethod == null) {
            throw new IllegalArgumentException("PaymentMethod is required");
        }
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Cannot pay for a cancelled booking");
        }

        double amountToPay = booking.getAmount();
        if (booking instanceof RecurringBooking recurringBooking) {
            List<TimeSlot> unpaid = recurringBooking.getUnpaidOccurrences();
            if (unpaid.isEmpty()) {
                throw new IllegalStateException("Booking is already paid");
            }
            Maid maid = maidService.getMaid(booking.getMaidId());
            amountToPay = unpaid.stream()
                    .mapToDouble(slot -> slot.durationInHours() * maid.getHourlyRate())
                    .sum();
        } else if (booking.getStatus() == BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Booking is already paid");
        }

        boolean success = paymentMethod.pay(amountToPay);
        if (!success) {
            Payment failed = new Payment(bookingId, amountToPay, paymentMethod, PaymentStatus.FAILED);
            return paymentRepository.save(failed);
        }

        if (booking instanceof RecurringBooking recurringBooking) {
            recurringBooking.markAllOccurrencesPaid();
        }
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        Payment payment = new Payment(bookingId, amountToPay, paymentMethod, PaymentStatus.SUCCESS);
        return paymentRepository.save(payment);
    }

    public Payment payForOccurrence(String bookingId, int occurrenceIndex, PaymentMethod paymentMethod) {
        if (paymentMethod == null) {
            throw new IllegalArgumentException("PaymentMethod is required");
        }
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));

        if (!(booking instanceof RecurringBooking recurringBooking)) {
            throw new IllegalArgumentException("Per-occurrence payment is only supported for RecurringBooking");
        }
        if (recurringBooking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Cannot pay for a cancelled booking");
        }

        TimeSlot slot = recurringBooking.getOccurrence(occurrenceIndex);
        if (recurringBooking.isOccurrencePaid(slot)) {
            throw new IllegalStateException("Occurrence is already paid");
        }

        Maid maid = maidService.getMaid(recurringBooking.getMaidId());
        double slotAmount = slot.durationInHours() * maid.getHourlyRate();

        boolean success = paymentMethod.pay(slotAmount);
        if (!success) {
            Payment failed = new Payment(bookingId, slotAmount, paymentMethod, PaymentStatus.FAILED);
            return paymentRepository.save(failed);
        }

        recurringBooking.markOccurrencePaid(slot);
        recurringBooking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(recurringBooking);

        Payment payment = new Payment(bookingId, slotAmount, paymentMethod, PaymentStatus.SUCCESS);
        return paymentRepository.save(payment);
    }
}
