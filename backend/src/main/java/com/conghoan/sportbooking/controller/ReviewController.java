package com.conghoan.sportbooking.controller;

import com.conghoan.sportbooking.dto.ApiResponse;
import com.conghoan.sportbooking.entity.Review;
import com.conghoan.sportbooking.entity.User;
import com.conghoan.sportbooking.entity.Venue;
import com.conghoan.sportbooking.repository.ReviewRepository;
import com.conghoan.sportbooking.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final VenueRepository venueRepository;

    @GetMapping("/venue/{venueId}")
    public ResponseEntity<ApiResponse<List<Review>>> getByVenue(@PathVariable Long venueId) {
        List<Review> reviews = reviewRepository.findByVenueIdOrderByCreatedAtDesc(venueId);
        return ResponseEntity.ok(ApiResponse.ok(reviews));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Review>> create(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, Object> body) {
        try {
            Long venueId = Long.valueOf(body.get("venueId").toString());
            Integer rating = Integer.valueOf(body.get("rating").toString());
            String comment = (String) body.get("comment");

            if (rating < 1 || rating > 5) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Rating phải từ 1 đến 5"));
            }

            Venue venue = venueRepository.findById(venueId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sân"));

            Review review = Review.builder()
                    .user(user)
                    .venue(venue)
                    .rating(rating)
                    .comment(comment)
                    .build();
            review = reviewRepository.save(review);

            // Cập nhật rating trung bình của venue
            List<Review> allReviews = reviewRepository.findByVenueIdOrderByCreatedAtDesc(venueId);
            double avgRating = allReviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
            venue.setRating(Math.round(avgRating * 10.0) / 10.0);
            venue.setRatingCount(allReviews.size());
            venueRepository.save(venue);

            return ResponseEntity.ok(ApiResponse.ok("Đánh giá thành công", review));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        try {
            Review review = reviewRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá"));

            if (!review.getUser().getId().equals(user.getId())) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Bạn không có quyền xoá đánh giá này"));
            }

            Long venueId = review.getVenue().getId();
            reviewRepository.delete(review);

            // Cập nhật lại rating của venue
            Venue venue = venueRepository.findById(venueId).orElse(null);
            if (venue != null) {
                List<Review> remaining = reviewRepository.findByVenueIdOrderByCreatedAtDesc(venueId);
                if (remaining.isEmpty()) {
                    venue.setRating(5.0);
                    venue.setRatingCount(0);
                } else {
                    double avgRating = remaining.stream()
                            .mapToInt(Review::getRating)
                            .average()
                            .orElse(5.0);
                    venue.setRating(Math.round(avgRating * 10.0) / 10.0);
                    venue.setRatingCount(remaining.size());
                }
                venueRepository.save(venue);
            }

            return ResponseEntity.ok(ApiResponse.ok("Xoá đánh giá thành công", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
