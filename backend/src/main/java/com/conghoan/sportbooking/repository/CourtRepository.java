package com.conghoan.sportbooking.repository;

import com.conghoan.sportbooking.entity.Court;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CourtRepository extends JpaRepository<Court, Long> {
    List<Court> findByVenueIdAndActiveTrue(Long venueId);
}
