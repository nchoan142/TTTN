package com.conghoan.sportbooking.repository;

import com.conghoan.sportbooking.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VenueRepository extends JpaRepository<Venue, Long> {
    List<Venue> findByCategoryIdAndActiveTrue(Long categoryId);
    List<Venue> findByActiveTrue();
    List<Venue> findByNameContainingIgnoreCaseAndActiveTrue(String name);
}
