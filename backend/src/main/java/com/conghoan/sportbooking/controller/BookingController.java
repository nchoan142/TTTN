package com.conghoan.sportbooking.controller;

import com.conghoan.sportbooking.dto.ApiResponse;
import com.conghoan.sportbooking.dto.BookingRequest;
import com.conghoan.sportbooking.entity.Booking;
import com.conghoan.sportbooking.entity.User;
import com.conghoan.sportbooking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<ApiResponse<Booking>> create(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody BookingRequest request) {
        try {
            Booking booking = bookingService.createBooking(user, request);
            return ResponseEntity.ok(ApiResponse.ok("Đặt lịch thành công", booking));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMyBookings(@AuthenticationPrincipal User user) {
        List<Booking> bookings = bookingService.getUserBookings(user.getId());
        List<Map<String, Object>> result = bookings.stream().map(b -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", b.getId());
            map.put("bookingDate", b.getBookingDate() != null ? b.getBookingDate().toString() : "");
            map.put("startTime", b.getStartTime() != null ? b.getStartTime().toString() : "");
            map.put("endTime", b.getEndTime() != null ? b.getEndTime().toString() : "");
            map.put("status", b.getStatus() != null ? b.getStatus().name() : "");
            map.put("totalPrice", b.getTotalPrice());
            map.put("note", b.getNote());
            map.put("courtName", b.getCourt() != null ? b.getCourt().getName() : "");
            map.put("venueName", b.getCourt() != null && b.getCourt().getVenue() != null ? b.getCourt().getVenue().getName() : "");
            map.put("createdAt", b.getCreatedAt() != null ? b.getCreatedAt().toString() : "");
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Booking>> cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        try {
            Booking booking = bookingService.cancelBooking(id, user);
            return ResponseEntity.ok(ApiResponse.ok("Huỷ booking thành công", booking));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
