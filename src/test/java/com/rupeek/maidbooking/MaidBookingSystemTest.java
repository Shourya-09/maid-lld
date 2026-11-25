package com.rupeek.maidbooking;

import com.rupeek.maidbooking.model.booking.Booking;
import com.rupeek.maidbooking.model.booking.BookingStatus;
import com.rupeek.maidbooking.model.booking.InstantBooking;
import com.rupeek.maidbooking.model.booking.RecurringBooking;
import com.rupeek.maidbooking.model.booking.ScheduledBooking;
import com.rupeek.maidbooking.model.maid.AvailabilityWindow;
import com.rupeek.maidbooking.model.maid.Gender;
import com.rupeek.maidbooking.model.maid.Maid;
import com.rupeek.maidbooking.model.maid.ServiceType;
import com.rupeek.maidbooking.model.maid.TimeSlot;
import com.rupeek.maidbooking.model.payment.Payment;
import com.rupeek.maidbooking.model.payment.PaymentStatus;
import com.rupeek.maidbooking.model.user.User;
import com.rupeek.maidbooking.repository.InMemoryBookingRepository;
import com.rupeek.maidbooking.repository.InMemoryMaidRepository;
import com.rupeek.maidbooking.repository.InMemoryPaymentRepository;
import com.rupeek.maidbooking.repository.InMemoryUserRepository;
import com.rupeek.maidbooking.service.BookingService;
import com.rupeek.maidbooking.service.MaidService;
import com.rupeek.maidbooking.service.PaymentService;
import com.rupeek.maidbooking.strategy.filter.GenderFilter;
import com.rupeek.maidbooking.strategy.filter.LocalityFilter;
import com.rupeek.maidbooking.strategy.filter.MaidFilter;
import com.rupeek.maidbooking.strategy.filter.PriceFilter;
import com.rupeek.maidbooking.strategy.filter.RatingFilter;
import com.rupeek.maidbooking.strategy.filter.SkillFilter;
import com.rupeek.maidbooking.strategy.payment.CreditCardPaymentStrategy;
import com.rupeek.maidbooking.strategy.payment.UpiPaymentStrategy;
import com.rupeek.maidbooking.strategy.payment.WalletPaymentStrategy;
import com.rupeek.maidbooking.strategy.refund.TimeBasedRefundPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaidBookingSystemTest {

    private MaidService maidService;
    private BookingService bookingService;
    private PaymentService paymentService;
    private InMemoryPaymentRepository paymentRepo;
    private InMemoryUserRepository userRepo;

    private Maid asha;
    private Maid priya;
    private User user;
    // Monday 2026-11-30 10:00 to 12:00
    private final LocalDateTime monday10am = LocalDateTime.of(2026, 11, 30, 10, 0);

    @BeforeEach
    void setUp() {
        InMemoryMaidRepository maidRepo = new InMemoryMaidRepository();
        InMemoryBookingRepository bookingRepo = new InMemoryBookingRepository();
        paymentRepo = new InMemoryPaymentRepository();
        userRepo = new InMemoryUserRepository();

        maidService = new MaidService(maidRepo, bookingRepo);
        bookingService = new BookingService(
                maidService,
                bookingRepo,
                userRepo,
                paymentRepo,
                new TimeBasedRefundPolicy()
        );
        paymentService = new PaymentService(bookingRepo, paymentRepo, maidService);

        user = userRepo.save(new User("user-1", "Rahul", "rahul@upi", "4111-2222-3333-4444", "wallet-rahul-99"));
        userRepo.save(new User("user-2", "Sneha", "sneha@upi", "5111-2222-3333-4444", "wallet-sneha-01"));

        List<AvailabilityWindow> availability = List.of(
                new AvailabilityWindow(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(18, 0)),
                new AvailabilityWindow(DayOfWeek.WEDNESDAY, LocalTime.of(8, 0), LocalTime.of(18, 0)),
                new AvailabilityWindow(DayOfWeek.FRIDAY, LocalTime.of(8, 0), LocalTime.of(18, 0)),
                new AvailabilityWindow(LocalDateTime.now().getDayOfWeek(), LocalTime.of(0, 0), LocalTime.of(23, 59))
        );

        asha = maidService.onboardMaid(new Maid(
                "Asha", "Indiranagar", Gender.FEMALE, 4.8, 200.0,
                Set.of(ServiceType.CLEANING, ServiceType.COOKING),
                availability
        ));

        priya = maidService.onboardMaid(new Maid(
                "Priya", "Indiranagar", Gender.FEMALE, 4.2, 350.0,
                Set.of(ServiceType.CLEANING, ServiceType.DISHWASHING),
                availability
        ));
    }

    @Test
    void testDiscoveryWithFilters() {
        TimeSlot slot = new TimeSlot(monday10am, monday10am.plusHours(2));
        List<MaidFilter> filters = List.of(
                new LocalityFilter("Indiranagar"),
                new SkillFilter(ServiceType.CLEANING),
                new RatingFilter(4.5),
                new PriceFilter(250.0),
                new GenderFilter(Gender.FEMALE)
        );

        List<Maid> matches = maidService.searchMaids(slot, filters);
        assertEquals(1, matches.size());
        assertEquals(asha.getId(), matches.get(0).getId());
    }

    @Test
    void testInstantAndScheduledBookingsAndDoubleBookingPrevention() {
        // InstantBooking books right now for 1 hour
        Booking instantBooking = new InstantBooking(user.getId(), asha.getId(), ServiceType.CLEANING, 1);
        Booking savedInstant = bookingService.createBooking(instantBooking);
        assertEquals(1, savedInstant.getSlots().size());
        assertEquals(200.0, savedInstant.getAmount());

        // ScheduledBooking on Monday 10:00 - 12:00
        Booking scheduledBooking = new ScheduledBooking(
                user.getId(), asha.getId(), ServiceType.CLEANING, monday10am, monday10am.plusHours(2)
        );
        bookingService.createBooking(scheduledBooking);

        // Overlapping ScheduledBooking (11:00 - 13:00) on same maid must fail
        Booking overlappingBooking = new ScheduledBooking(
                "user-2", asha.getId(), ServiceType.COOKING,
                monday10am.plusHours(1), monday10am.plusHours(3)
        );
        assertThrows(IllegalStateException.class, () -> bookingService.createBooking(overlappingBooking));

        // Past-time ScheduledBooking must fail validation
        assertThrows(IllegalArgumentException.class, () -> new ScheduledBooking(
                user.getId(), asha.getId(), ServiceType.CLEANING,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(1).plusHours(2)
        ));
    }

    @Test
    void testRecurringBookingPerOccurrencePaymentAndSingleOccurrenceCancellation() {
        RecurringBooking recurring = new RecurringBooking(
                user.getId(), asha.getId(), ServiceType.COOKING,
                monday10am, 2,
                Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY), 3
        );
        Booking recurringBooking = bookingService.createBooking(recurring);
        assertEquals(3, recurringBooking.getSlots().size());
        assertEquals(1200.0, recurringBooking.getAmount());

        // Pay per occurrence for 1st (index 0) and 2nd (index 1) occurrences
        UpiPaymentStrategy upiStrategy = new UpiPaymentStrategy(user);
        WalletPaymentStrategy walletStrategy = new WalletPaymentStrategy(user);

        Payment p1 = paymentService.payForOccurrence(recurringBooking.getId(), 0, upiStrategy);
        Payment p2 = paymentService.payForOccurrence(recurringBooking.getId(), 1, walletStrategy);
        assertEquals(400.0, p1.getAmount());
        assertEquals(400.0, p2.getAmount());
        assertEquals(BookingStatus.CONFIRMED, recurringBooking.getStatus());

        // Cancel only the 2nd occurrence (index 1 -> Wednesday, >24h in future) -> 100% refund of that slot (400.0)
        LocalDateTime wednesday10am = monday10am.plusDays(2);
        double refund = bookingService.cancelRecurringOccurrence(recurringBooking.getId(), 1);
        assertEquals(400.0, refund);
        assertEquals(2, recurringBooking.getSlots().size());

        // Wednesday 10:00-12:00 is now free and bookable again, while Monday 10:00-12:00 is still busy
        assertTrue(maidService.isMaidAvailable(asha, new TimeSlot(wednesday10am, wednesday10am.plusHours(2))));
        assertFalse(maidService.isMaidAvailable(asha, new TimeSlot(monday10am, monday10am.plusHours(2))));
    }

    @Test
    void testFullCancellationUpdatesPaymentStatusToRefunded() {
        Booking scheduled = new ScheduledBooking(
                user.getId(), asha.getId(), ServiceType.CLEANING,
                monday10am, monday10am.plusHours(2)
        );
        Booking booking = bookingService.createBooking(scheduled);

        CreditCardPaymentStrategy ccStrategy = new CreditCardPaymentStrategy(user);
        Payment payment = paymentService.payForBooking(booking.getId(), ccStrategy);
        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());

        // Cancel >24 hours before slot start -> 100% refund (400.0) and PaymentStatus becomes REFUNDED
        double refund = bookingService.cancelBooking(booking.getId());
        assertEquals(400.0, refund);
        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        assertEquals(PaymentStatus.REFUNDED, paymentRepo.findByBookingId(booking.getId()).get(0).getStatus());

        // Slot is freed after cancellation
        assertTrue(maidService.isMaidAvailable(asha, new TimeSlot(monday10am, monday10am.plusHours(2))));
    }

    @Test
    void testConcurrentBookingOnSameMaidAndSlotAllowsOnlyOneWinner() throws InterruptedException {
        int threads = 10;
        for (int i = 0; i < threads; i++) {
            userRepo.save(new User("concurrent-user-" + i, "User" + i, "u@upi", "1234", "w1"));
        }

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch readyLatch = new CountDownLatch(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);

        for (int i = 0; i < threads; i++) {
            final String userId = "concurrent-user-" + i;
            executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    Booking req = new ScheduledBooking(
                            userId, asha.getId(), ServiceType.CLEANING,
                            monday10am, monday10am.plusHours(2)
                    );
                    bookingService.createBooking(req);
                    successCount.incrementAndGet();
                } catch (IllegalStateException e) {
                    conflictCount.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        assertEquals(1, successCount.get(), "Exactly one concurrent booking should succeed");
        assertEquals(threads - 1, conflictCount.get(), "All other concurrent bookings must fail with conflict");
    }
}
