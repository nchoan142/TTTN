package com.conghoan.sportbooking.controller;

import com.conghoan.sportbooking.dto.ApiResponse;
import com.conghoan.sportbooking.entity.Court;
import com.conghoan.sportbooking.entity.TimeSlot;
import com.conghoan.sportbooking.entity.Venue;
import com.conghoan.sportbooking.repository.CourtRepository;
import com.conghoan.sportbooking.repository.TimeSlotRepository;
import com.conghoan.sportbooking.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/venues")
@RequiredArgsConstructor
public class VenueController {

    private final VenueRepository venueRepository;
    private final CourtRepository courtRepository;
    private final TimeSlotRepository timeSlotRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Venue>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(venueRepository.findByActiveTrue()));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<Venue>>> getByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(ApiResponse.ok(venueRepository.findByCategoryIdAndActiveTrue(categoryId)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Venue>>> search(@RequestParam String q) {
        return ResponseEntity.ok(ApiResponse.ok(venueRepository.findByNameContainingIgnoreCaseAndActiveTrue(q)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Venue>> getById(@PathVariable Long id) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sân"));
        return ResponseEntity.ok(ApiResponse.ok(venue));
    }

    @GetMapping("/{id}/courts")
    public ResponseEntity<ApiResponse<List<Court>>> getCourts(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(courtRepository.findByVenueIdAndActiveTrue(id)));
    }

    @GetMapping("/{id}/slots")
    public ResponseEntity<ApiResponse<List<TimeSlot>>> getSlots(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.ok(timeSlotRepository.findByCourtVenueIdAndDate(id, date)));
    }
}
