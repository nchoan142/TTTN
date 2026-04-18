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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final VenueRepository venueRepository;
    private final BookingRepository bookingRepository;
    private final SportCategoryRepository categoryRepository;
    private final CourtRepository courtRepository;
    private final TimeSlotRepository timeSlotRepository;
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

    @PostMapping("/venues")
    public ResponseEntity<ApiResponse<Venue>> createVenue(@RequestBody Map<String, Object> body) {
        try {
            String name = (String) body.get("name");
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Tên sân không được để trống"));
            }

            Venue.VenueBuilder builder = Venue.builder()
                    .name(name)
                    .address((String) body.get("address"))
                    .phone((String) body.get("phone"))
                    .imageUrl((String) body.get("imageUrl"))
                    .rating(5.0)
                    .ratingCount(0)
                    .active(true);

            // Open/close time (HH:mm)
            String openTime = (String) body.get("openTime");
            if (openTime != null && !openTime.isEmpty()) {
                builder.openTime(java.time.LocalTime.parse(openTime));
            }
            String closeTime = (String) body.get("closeTime");
            if (closeTime != null && !closeTime.isEmpty()) {
                builder.closeTime(java.time.LocalTime.parse(closeTime));
            }

            // Price per slot
            Object priceObj = body.get("pricePerSlot");
            if (priceObj instanceof Number) {
                builder.pricePerSlot(((Number) priceObj).doubleValue());
            }

            // Category
            Object categoryIdObj = body.get("categoryId");
            if (categoryIdObj instanceof Number) {
                Long categoryId = ((Number) categoryIdObj).longValue();
                SportCategory category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));
                builder.category(category);
            }

            // Owner mặc định: user đầu tiên có role OWNER
            User owner = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == User.Role.OWNER)
                    .findFirst()
                    .orElse(null);
            if (owner != null) {
                builder.owner(owner);
            }

            Venue venue = venueRepository.save(builder.build());

            // Tạo sân con mặc định nếu có courtNames
            Object courtNamesObj = body.get("courtNames");
            if (courtNamesObj instanceof List) {
                List<?> courtNames = (List<?>) courtNamesObj;
                for (Object cn : courtNames) {
                    if (cn != null && !cn.toString().trim().isEmpty()) {
                        Court court = Court.builder()
                                .name(cn.toString())
                                .venue(venue)
                                .active(true)
                                .build();
                        courtRepository.save(court);
                    }
                }
            }

            return ResponseEntity.ok(ApiResponse.ok("Tạo sân thành công", venue));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/venues/{id}")
    public ResponseEntity<ApiResponse<Venue>> updateVenue(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        try {
            Venue venue = venueRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sân"));

            if (body.containsKey("name")) venue.setName((String) body.get("name"));
            if (body.containsKey("address")) venue.setAddress((String) body.get("address"));
            if (body.containsKey("phone")) venue.setPhone((String) body.get("phone"));
            if (body.containsKey("imageUrl")) venue.setImageUrl((String) body.get("imageUrl"));

            String openTime = (String) body.get("openTime");
            if (openTime != null && !openTime.isEmpty()) venue.setOpenTime(java.time.LocalTime.parse(openTime));
            String closeTime = (String) body.get("closeTime");
            if (closeTime != null && !closeTime.isEmpty()) venue.setCloseTime(java.time.LocalTime.parse(closeTime));

            Object priceObj = body.get("pricePerSlot");
            if (priceObj instanceof Number) venue.setPricePerSlot(((Number) priceObj).doubleValue());

            Object categoryIdObj = body.get("categoryId");
            if (categoryIdObj instanceof Number) {
                Long categoryId = ((Number) categoryIdObj).longValue();
                SportCategory category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));
                venue.setCategory(category);
            }

            venueRepository.save(venue);
            return ResponseEntity.ok(ApiResponse.ok("Cập nhật sân thành công", venue));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/venues/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVenue(@PathVariable Long id) {
        try {
            Venue venue = venueRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sân"));
            venueRepository.delete(venue);
            return ResponseEntity.ok(ApiResponse.ok("Xoá sân thành công", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
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

//    @GetMapping("/bookings")
//    public ResponseEntity<ApiResponse<List<Booking>>> getAllBookings() {
//        return ResponseEntity.ok(ApiResponse.ok(bookingRepository.findAll()));
//    }

    @GetMapping("/bookings")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();

        List<Map<String, Object>> result = bookings.stream().map(b -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", b.getId());
            map.put("status", b.getStatus().name());
            map.put("bookingDate", b.getBookingDate().toString());
            map.put("startTime", b.getStartTime().toString());
            map.put("endTime", b.getEndTime().toString());
            map.put("totalPrice", b.getTotalPrice());

            // Thông tin User
            if (b.getUser() != null) {
                map.put("userName", b.getUser().getFullName());
            }

            // Xử lý lồng ghép dữ liệu để Android Adapter đọc được
            Map<String, Object> courtMap = new HashMap<>();
            if (b.getCourt() != null) {
                courtMap.put("name", b.getCourt().getName());

                Venue v = b.getCourt().getVenue();
                if (v != null) {
                    Map<String, Object> venueMap = new HashMap<>();
                    venueMap.put("name", v.getName());

                    // Lấy thông tin Category
                    if (v.getCategory() != null) {
                        Map<String, Object> catMap = new HashMap<>();
                        catMap.put("name", v.getCategory().getName());
                        venueMap.put("category", catMap);
                    }
                    courtMap.put("venue", venueMap);
                }
            }
            map.put("court", courtMap);

            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PutMapping("/bookings/{id}/status")
    @org.springframework.transaction.annotation.Transactional
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

            // Khi admin xác nhận -> đảm bảo time slots là BOOKED
            if (status == Booking.BookingStatus.CONFIRMED) {
                List<TimeSlot> slots = timeSlotRepository.findByCourtIdAndDate(
                        booking.getCourt().getId(), booking.getBookingDate());
                for (TimeSlot slot : slots) {
                    if (slot.getBooking() != null && booking.getId().equals(slot.getBooking().getId())) {
                        if (slot.getStatus() != TimeSlot.SlotStatus.BOOKED) {
                            slot.setStatus(TimeSlot.SlotStatus.BOOKED);
                            timeSlotRepository.save(slot);
                        }
                    }
                }
            }

            // Khi admin huỷ lịch -> giải phóng các time slots đã đặt
            if (status == Booking.BookingStatus.CANCELLED) {
                List<TimeSlot> slots = timeSlotRepository.findByCourtIdAndDate(
                        booking.getCourt().getId(), booking.getBookingDate());
                for (TimeSlot slot : slots) {
                    if (slot.getBooking() != null && booking.getId().equals(slot.getBooking().getId())) {
                        slot.setStatus(TimeSlot.SlotStatus.AVAILABLE);
                        slot.setBooking(null);
                        timeSlotRepository.save(slot);
                    }
                }
            }

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