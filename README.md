# Maid Booking System (Rupeek SDE-3 Machine Coding)

A clean, modular Java 17 + Spring Boot backend service for discovering, onboarding, booking, paying for, and cancelling home-service maids.

## 1. Package Structure

```
com.rupeek.maidbooking
├── MaidBookingApplication.java
├── model/
│   ├── booking/    # abstract Booking, InstantBooking, ScheduledBooking, RecurringBooking, BookingRequest, enums
│   ├── maid/       # Maid, AvailabilityWindow, TimeSlot, ServiceType, Gender
│   ├── payment/    # Payment, PaymentMethod (interface), RefundPolicy (interface), PaymentStatus
│   └── user/       # User (with upiId, creditCardNumber, walletId)
├── repository/     # Repository interfaces and thread-safe in-memory implementations
├── strategy/
│   ├── filter/     # MaidFilter interface + LocalityFilter, SkillFilter, RatingFilter, PriceFilter, GenderFilter
│   ├── payment/    # UpiPaymentStrategy, CreditCardPaymentStrategy, WalletPaymentStrategy
│   └── refund/     # TimeBasedRefundPolicy
├── service/        # MaidService, BookingService (with per-maid ReentrantLock), PaymentService
└── controller/     # MaidBookingController & GlobalExceptionHandler
```

## 2. Key Design Decisions & Extensibility

1. **Booking Hierarchy (`abstract class Booking`)**:
   - `Booking` is an abstract class defining common booking state and `abstract List<TimeSlot> getSlots()`.
   - Three concrete classes extend `Booking`:
     - `InstantBooking`: books a maid immediately (`LocalDateTime.now()`) for `durationHours`.
     - `ScheduledBooking`: books a maid for a specific future `[startTime, endTime]`.
     - `RecurringBooking`: books repeating occurrence slots across `recurringDays` for `occurrencesCount` occurrences and tracks per-occurrence payment and single-occurrence cancellation.
   - Adding a new booking type only requires extending `Booking`.
2. **Payment Method Interface & User Payment Details (`PaymentMethod` + `User`)**:
   - `User` holds user details including `upiId`, `creditCardNumber`, and `walletId`.
   - `Payment` holds a `PaymentMethod` interface reference (`boolean pay(double amount)`), implemented by:
     - `UpiPaymentStrategy` (`getUpiId()`)
     - `CreditCardPaymentStrategy` (`getCreditCardNumber()`)
     - `WalletPaymentStrategy` (`getWalletId()`)
   - Supports paying for the full booking (`payForBooking`) or per occurrence (`payForOccurrence`) for recurring bookings.
3. **Composable Maid Discovery (`MaidFilter`)**:
   - `MaidFilter` (`boolean matches(Maid maid)`) is implemented by `LocalityFilter`, `SkillFilter`, `RatingFilter`, `PriceFilter`, and `GenderFilter`.
   - New filters can be added as new `MaidFilter` implementations without modifying `MaidService.searchMaids`.
   - Only maids whose `AvailabilityWindow` covers the requested `TimeSlot` and who have no overlapping active bookings are returned.
4. **Concurrency & Double-Booking Prevention**:
   - `BookingService` uses a per-maid `ReentrantLock` (`ConcurrentHashMap<String, ReentrantLock>`) around availability checks and booking persistence so concurrent requests for the same maid cannot double-book overlapping slots.
5. **Cancellation & Pluggable Refund Policy (`RefundPolicy`)**:
   - Supports cancelling either a **single occurrence** of a `RecurringBooking` or the **entire booking / series**, immediately freeing the maid's slot(s) and marking refunded payments as `PaymentStatus.REFUNDED`.
   - `TimeBasedRefundPolicy`: $\ge 24$ hours before slot $\rightarrow 100\%$ refund; $2\text{--}24$ hours $\rightarrow 50\%$ refund; $< 2$ hours $\rightarrow 0\%$ refund.

## 3. Key Assumptions

- Each `TimeSlot` falls within a single calendar day and is matched against the maid's `AvailabilityWindow` for that `DayOfWeek`.
- Pricing is calculated as `slot.durationInHours() * maid.getHourlyRate()`.
- Payment gateways are mocked inside `UpiPaymentStrategy`, `CreditCardPaymentStrategy`, and `WalletPaymentStrategy` by validating the user's corresponding credential (`upiId`, `creditCardNumber`, `walletId`).

## 4. How to Build and Run

### Run Unit Tests
```bash
mvn test
```

### Run the Spring Boot Service
```bash
mvn spring-boot:run
```
