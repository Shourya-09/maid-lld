package com.rupeek.maidbooking.controller;

import com.rupeek.maidbooking.model.booking.Booking;
import com.rupeek.maidbooking.model.booking.BookingRequest;
import com.rupeek.maidbooking.model.maid.Gender;
import com.rupeek.maidbooking.model.maid.Maid;
import com.rupeek.maidbooking.model.maid.ServiceType;
import com.rupeek.maidbooking.model.maid.TimeSlot;
import com.rupeek.maidbooking.model.payment.Payment;
import com.rupeek.maidbooking.model.payment.PaymentMethod;
import com.rupeek.maidbooking.model.user.User;
import com.rupeek.maidbooking.repository.UserRepository;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MaidBookingController {
    private final MaidService maidService;
    private final BookingService bookingService;
    private final PaymentService paymentService;
    private final UserRepository userRepository;

    public MaidBookingController(MaidService maidService,
                                 BookingService bookingService,
                                 PaymentService paymentService,
                                 UserRepository userRepository) {
        this.maidService = maidService;
        this.bookingService = bookingService;
        this.paymentService = paymentService;
        this.userRepository = userRepository;
    }

    @PostMapping("/users")
    public User registerUser(@RequestBody User user) {
        return userRepository.save(user);
    }

    @PostMapping("/maids")
    public Maid onboardMaid(@RequestBody Maid maid) {
        return maidService.onboardMaid(maid);
    }

    @GetMapping("/maids/{id}")
    public Maid getMaid(@PathVariable String id) {
        return maidService.getMaid(id);
    }

    @GetMapping("/maids/search")
    public List<Maid> searchMaids(@RequestParam(required = false) String locality,
                                  @RequestParam(required = false) ServiceType skill,
                                  @RequestParam(required = false) Double minRating,
                                  @RequestParam(required = false) Double maxPrice,
                                  @RequestParam(required = false) Gender gender,
                                  @RequestParam(required = false) LocalDateTime startTime,
                                  @RequestParam(required = false) LocalDateTime endTime) {
        List<MaidFilter> filters = new ArrayList<>();
        if (locality != null) filters.add(new LocalityFilter(locality));
        if (skill != null) filters.add(new SkillFilter(skill));
        if (minRating != null) filters.add(new RatingFilter(minRating));
        if (maxPrice != null) filters.add(new PriceFilter(maxPrice));
        if (gender != null) filters.add(new GenderFilter(gender));

        TimeSlot slot = (startTime != null && endTime != null) ? new TimeSlot(startTime, endTime) : null;
        return maidService.searchMaids(slot, filters);
    }

    @PostMapping("/bookings")
    public Booking createBooking(@RequestBody BookingRequest request) {
        return bookingService.createBooking(request.toBooking());
    }

    @GetMapping("/bookings/{id}")
    public Booking getBooking(@PathVariable String id) {
        return bookingService.getBooking(id);
    }

    @PostMapping("/bookings/{id}/pay")
    public Payment payForBooking(@PathVariable String id,
                                 @RequestParam String method) {
        Booking booking = bookingService.getBooking(id);
        User user = userRepository.findById(booking.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + booking.getUserId()));
        return paymentService.payForBooking(id, resolvePaymentMethod(method, user));
    }

    @PostMapping("/bookings/{id}/occurrences/{index}/pay")
    public Payment payForOccurrence(@PathVariable String id,
                                    @PathVariable int index,
                                    @RequestParam String method) {
        Booking booking = bookingService.getBooking(id);
        User user = userRepository.findById(booking.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + booking.getUserId()));
        return paymentService.payForOccurrence(id, index, resolvePaymentMethod(method, user));
    }

    @PostMapping("/bookings/{id}/cancel")
    public Map<String, Object> cancelBooking(@PathVariable String id) {
        double refund = bookingService.cancelBooking(id);
        return Map.of("bookingId", id, "refundAmount", refund, "status", "CANCELLED");
    }

    @PostMapping("/bookings/{id}/occurrences/{index}/cancel")
    public Map<String, Object> cancelRecurringOccurrence(@PathVariable String id, @PathVariable int index) {
        double refund = bookingService.cancelRecurringOccurrence(id, index);
        return Map.of("bookingId", id, "cancelledOccurrenceIndex", index, "refundAmount", refund);
    }

    private PaymentMethod resolvePaymentMethod(String method, User user) {
        return switch (method.toUpperCase()) {
            case "UPI" -> new UpiPaymentStrategy(user);
            case "CARD", "CREDIT_CARD" -> new CreditCardPaymentStrategy(user);
            case "WALLET" -> new WalletPaymentStrategy(user);
            default -> throw new IllegalArgumentException("Unsupported payment method: " + method);
        };
    }
}
