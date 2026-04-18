package com.conghoan.sportbooking.controller;

import com.conghoan.sportbooking.dto.ApiResponse;
import com.conghoan.sportbooking.entity.Booking;
import com.conghoan.sportbooking.entity.Court;
import com.conghoan.sportbooking.entity.TimeSlot;
import com.conghoan.sportbooking.entity.Venue;
import com.conghoan.sportbooking.repository.BookingRepository;
import com.conghoan.sportbooking.repository.CourtRepository;
import com.conghoan.sportbooking.repository.TimeSlotRepository;
import com.conghoan.sportbooking.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/venues")
@RequiredArgsConstructor
public class VenueController {

    private final VenueRepository venueRepository;
    private final CourtRepository courtRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final BookingRepository bookingRepository;

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
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getSlots(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        // Lấy danh sách courts cho venue
        List<Court> courts = courtRepository.findByVenueIdAndActiveTrue(id);
        if (courts.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.ok(new ArrayList<>()));
        }

        // Map courtId → courtIndex
        Map<Long, Integer> courtIndexMap = new HashMap<>();
        Map<Long, String> courtNameMap = new HashMap<>();
        for (int i = 0; i < courts.size(); i++) {
            courtIndexMap.put(courts.get(i).getId(), i);
            courtNameMap.put(courts.get(i).getId(), courts.get(i).getName());
        }

        // Lấy venue info để biết giờ mở/đóng + giá
        Venue venue = venueRepository.findById(id).orElse(null);
        LocalTime openTime = venue != null && venue.getOpenTime() != null ? venue.getOpenTime() : LocalTime.of(5, 0);
        LocalTime closeTime = venue != null && venue.getCloseTime() != null ? venue.getCloseTime() : LocalTime.of(23, 0);
        double pricePerSlot = venue != null && venue.getPricePerSlot() != null ? venue.getPricePerSlot() : 0;

        // Tính danh sách time indices (mỗi 30 phút)
        List<LocalTime> timeList = new ArrayList<>();
        LocalTime t = openTime;
        while (t.isBefore(closeTime)) {
            timeList.add(t);
            t = t.plusMinutes(30);
        }

        // Lấy slots từ DB
        List<TimeSlot> dbSlots = timeSlotRepository.findByCourtVenueIdAndDate(id, date);
        // Map (courtId, startTime) → status
        Map<String, String> slotStatusMap = new HashMap<>();
        for (TimeSlot slot : dbSlots) {
            String key = slot.getCourt().getId() + "_" + slot.getStartTime().toString();
            slotStatusMap.put(key, slot.getStatus().name());
        }

        // Lấy status trong bảng bookings
        List<Booking> confirmedBookings = bookingRepository.findByCourtVenueIdAndBookingDate(id, date);
        Map<String, String> liveStatusMap = new HashMap<>();

        for (Booking b : confirmedBookings) {
            // Kiểm tra trạng thái CONFIRMED từ Enum BookingStatus
            // Nếu có trong Bookings thì lấy CONFIRMED, không thì lấy từ TimeSlots
            if (b.getStatus() == Booking.BookingStatus.CONFIRMED) {
                LocalTime slotT = b.getStartTime();
                while (slotT.isBefore(b.getEndTime())) {
                    String key = b.getCourt().getId() + "_" + slotT.toString();
                    liveStatusMap.put(key, "CONFIRMED");
                    slotT = slotT.plusMinutes(30);
                }
            }
        }

        // Build flat list cho Android
        List<Map<String, Object>> result = new ArrayList<>();
        for (int ci = 0; ci < courts.size(); ci++) {
            Court court = courts.get(ci);
            for (int ti = 0; ti < timeList.size(); ti++) {
                LocalTime slotTime = timeList.get(ti);
                String key = court.getId() + "_" + slotTime.toString();
//                String status = slotStatusMap.getOrDefault(key, "AVAILABLE");

                String status = liveStatusMap.getOrDefault(key,
                        slotStatusMap.getOrDefault(key, "AVAILABLE"));

                Map<String, Object> slotMap = new HashMap<>();
                slotMap.put("courtIndex", ci);
                slotMap.put("timeIndex", ti);
                slotMap.put("courtId", court.getId());
                slotMap.put("courtName", court.getName());
                slotMap.put("startTime", slotTime.toString());
                slotMap.put("endTime", slotTime.plusMinutes(30).toString());
                slotMap.put("status", status);
                slotMap.put("price", pricePerSlot);
                result.add(slotMap);
            }
        }

        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}