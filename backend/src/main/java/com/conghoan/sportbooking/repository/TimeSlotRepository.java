package com.conghoan.sportbooking.repository;

import com.conghoan.sportbooking.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    List<TimeSlot> findByCourtIdAndDate(Long courtId, LocalDate date);
    List<TimeSlot> findByCourtVenueIdAndDate(Long venueId, LocalDate date);
}
