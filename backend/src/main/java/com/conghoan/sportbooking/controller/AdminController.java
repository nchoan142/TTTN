package com.conghoan.sportbooking.controller;

import com.conghoan.sportbooking.dto.ApiResponse;
import com.conghoan.sportbooking.entity.*;
import com.conghoan.sportbooking.repository.*;
import com.conghoan.sportbooking.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final VenueRepository venueRepository;
    private final BookingRepository bookingRepository;
    private final SportCategoryRepository categoryRepository;
    private final CloudinaryService cloudinaryService;

    @Autowired
    private DataSource dataSource;

    // ==================== STATS ====================

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalVenues", venueRepository.count());

        List<Booking> allBookings = bookingRepository.findAll();
        stats.put("totalBookings", allBookings.size());

        double totalRevenue = allBookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.CONFIRMED
                        || b.getStatus() == Booking.BookingStatus.COMPLETED)
                .mapToDouble(b -> b.getTotalPrice() != null ? b.getTotalPrice() : 0)
                .sum();
        stats.put("totalRevenue", totalRevenue);

        Map<String, Long> bookingsByStatus = new HashMap<>();
        for (Booking.BookingStatus status : Booking.BookingStatus.values()) {
            long count = allBookings.stream()
                    .filter(b -> b.getStatus() == status)
                    .count();
            bookingsByStatus.put(status.name(), count);
        }
        stats.put("bookingsByStatus", bookingsByStatus);

        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    // ==================== USERS ====================

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.ok(userRepository.findAll()));
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse<User>> changeUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

            String roleName = body.get("role");
            User.Role role = User.Role.valueOf(roleName);
            user.setRole(role);
            userRepository.save(user);

            return ResponseEntity.ok(ApiResponse.ok("Cập nhật role thành công", user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Role không hợp lệ"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
            userRepository.delete(user);
            return ResponseEntity.ok(ApiResponse.ok("Xoá người dùng thành công", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==================== VENUES ====================

    @GetMapping("/venues")
    public ResponseEntity<ApiResponse<List<Venue>>> getAllVenues() {
        return ResponseEntity.ok(ApiResponse.ok(venueRepository.findAll()));
    }

    @PutMapping("/venues/{id}/image")
    public ResponseEntity<ApiResponse<Venue>> updateVenueImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            Venue venue = venueRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sân"));

            Map<String, Object> uploadResult = cloudinaryService.uploadImage(file, "sportbooking/venues");
            venue.setImageUrl(uploadResult.get("url").toString());
            venueRepository.save(venue);

            return ResponseEntity.ok(ApiResponse.ok("Cập nhật ảnh sân thành công", venue));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/venues/{id}/toggle")
    public ResponseEntity<ApiResponse<Venue>> toggleVenue(@PathVariable Long id) {
        try {
            Venue venue = venueRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sân"));
            venue.setActive(!venue.isActive());
            venueRepository.save(venue);
            String msg = venue.isActive() ? "Kích hoạt sân thành công" : "Vô hiệu hoá sân thành công";
            return ResponseEntity.ok(ApiResponse.ok(msg, venue));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==================== CATEGORIES ====================

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<SportCategory>>> getAllCategories() {
        return ResponseEntity.ok(ApiResponse.ok(categoryRepository.findAll()));
    }

    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<SportCategory>> createCategory(@RequestBody Map<String, String> body) {
        try {
            SportCategory category = SportCategory.builder()
                    .name(body.get("name"))
                    .iconUrl(body.get("iconUrl"))
                    .active(true)
                    .build();
            category = categoryRepository.save(category);
            return ResponseEntity.ok(ApiResponse.ok("Tạo danh mục thành công", category));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<SportCategory>> updateCategory(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            SportCategory category = categoryRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));

            if (body.containsKey("name")) {
                category.setName(body.get("name"));
            }
            if (body.containsKey("iconUrl")) {
                category.setIconUrl(body.get("iconUrl"));
            }
            categoryRepository.save(category);

            return ResponseEntity.ok(ApiResponse.ok("Cập nhật danh mục thành công", category));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        try {
            SportCategory category = categoryRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
            categoryRepository.delete(category);
            return ResponseEntity.ok(ApiResponse.ok("Xoá danh mục thành công", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==================== BOOKINGS ====================

    @GetMapping("/bookings")
    public ResponseEntity<ApiResponse<List<Booking>>> getAllBookings() {
        return ResponseEntity.ok(ApiResponse.ok(bookingRepository.findAll()));
    }

    @PutMapping("/bookings/{id}/status")
    public ResponseEntity<ApiResponse<Booking>> updateBookingStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            Booking booking = bookingRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch đặt"));

            String statusName = body.get("status");
            Booking.BookingStatus status = Booking.BookingStatus.valueOf(statusName);
            booking.setStatus(status);
            bookingRepository.save(booking);

            return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái thành công", booking));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Trạng thái không hợp lệ"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==================== EXPORT DATABASE ====================

    @GetMapping("/export-sql")
    public ResponseEntity<String> exportSql() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SCRIPT")) {

            StringBuilder sb = new StringBuilder();
            sb.append("-- SportBooking Database Export\n");
            sb.append("-- Date: ").append(java.time.LocalDateTime.now()).append("\n\n");

            while (rs.next()) {
                sb.append(rs.getString(1)).append("\n");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.set("Content-Disposition", "attachment; filename=sportbooking_export.sql");

            return ResponseEntity.ok().headers(headers).body(sb.toString());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}
