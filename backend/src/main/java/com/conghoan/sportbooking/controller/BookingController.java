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

import java.util.List;

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
    public ResponseEntity<ApiResponse<List<Booking>>> getMyBookings(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.ok(bookingService.getUserBookings(user.getId())));
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
