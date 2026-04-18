package com.conghoan.sportbooking.repository;

import com.conghoan.sportbooking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Booking> findByCourtVenueId(Long venueId);
    List<Booking> findByCourtVenueIdAndBookingDate(Long venueId, LocalDate bookingDate);
}
