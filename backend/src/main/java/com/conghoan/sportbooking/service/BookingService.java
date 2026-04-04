package com.conghoan.sportbooking.service;

import com.conghoan.sportbooking.dto.BookingRequest;
import com.conghoan.sportbooking.entity.*;
import com.conghoan.sportbooking.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final CourtRepository courtRepository;
    private final TimeSlotRepository timeSlotRepository;

    @Transactional
    public Booking createBooking(User user, BookingRequest request) {
        Court court = courtRepository.findById(request.getCourtId())
                .orElseThrow(() -> new RuntimeException("Sân không tồn tại"));

        // Check slot availability
        List<TimeSlot> slots = timeSlotRepository.findByCourtIdAndDate(
                request.getCourtId(), request.getBookingDate());

        for (TimeSlot slot : slots) {
            if (!slot.getStartTime().isBefore(request.getStartTime()) ||
                    slot.getStartTime().isBefore(request.getEndTime())) {
                if (slot.getStatus() != TimeSlot.SlotStatus.AVAILABLE) {
                    if (isOverlapping(slot.getStartTime(), slot.getEndTime(),
                            request.getStartTime(), request.getEndTime())) {
                        throw new RuntimeException("Khung giờ đã được đặt hoặc khoá");
                    }
                }
            }
        }

        // Calculate price
        long durationMinutes = java.time.Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();
        long slotCount = durationMinutes / 30;
        double totalPrice = slotCount * (court.getVenue().getPricePerSlot() != null ? court.getVenue().getPricePerSlot() : 0);

        Booking booking = Booking.builder()
                .user(user)
                .court(court)
                .bookingDate(request.getBookingDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(Booking.BookingStatus.PENDING)
                .totalPrice(totalPrice)
                .note(request.getNote())
                .build();

        booking = bookingRepository.save(booking);

        // Update time slots
        for (TimeSlot slot : slots) {
            if (isOverlapping(slot.getStartTime(), slot.getEndTime(),
                    request.getStartTime(), request.getEndTime())) {
                if (slot.getStatus() == TimeSlot.SlotStatus.AVAILABLE) {
                    slot.setStatus(TimeSlot.SlotStatus.BOOKED);
                    slot.setBooking(booking);
                    timeSlotRepository.save(slot);
                }
            }
        }

        return booking;
    }

    public List<Booking> getUserBookings(Long userId) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public Booking cancelBooking(Long bookingId, User user) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại"));

        if (!booking.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Không có quyền huỷ booking này");
        }

        booking.setStatus(Booking.BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        // Free time slots
        List<TimeSlot> slots = timeSlotRepository.findByCourtIdAndDate(
                booking.getCourt().getId(), booking.getBookingDate());
        for (TimeSlot slot : slots) {
            if (booking.equals(slot.getBooking())) {
                slot.setStatus(TimeSlot.SlotStatus.AVAILABLE);
                slot.setBooking(null);
                timeSlotRepository.save(slot);
            }
        }

        return booking;
    }

    private boolean isOverlapping(LocalTime s1, LocalTime e1, LocalTime s2, LocalTime e2) {
        return s1.isBefore(e2) && s2.isBefore(e1);
    }
}
