package com.conghoan.sportbooking.config;

import com.conghoan.sportbooking.entity.*;
import com.conghoan.sportbooking.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SportCategoryRepository categoryRepository;
    private final VenueRepository venueRepository;
    private final CourtRepository courtRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        // Users
        User user = userRepository.save(User.builder()
                .fullName("Nguyễn Công Hoàn")
                .email("hoan@test.com")
                .password(passwordEncoder.encode("123456"))
//                .password("123456")
                .phone("0912345678")
                .role(User.Role.USER)
                .build());

        User owner = userRepository.save(User.builder()
                .fullName("Chủ sân Demo")
                .email("owner@test.com")
                .password(passwordEncoder.encode("123456"))
//                .password("123456")
                .phone("0987654321")
                .role(User.Role.OWNER)
                .build());

        User admin = userRepository.save(User.builder()
                .fullName("Admin System")
                .email("admin@test.com")
                .password(passwordEncoder.encode("123456"))
//                .password("123456")
                .phone("0900000000")
                .role(User.Role.ADMIN)
                .build());

        // Sport Categories
        SportCategory pickleball = categoryRepository.save(SportCategory.builder().name("Pickleball").iconUrl("https://img.icons8.com/color/96/ping-pong.png").active(true).build());
        SportCategory caulong = categoryRepository.save(SportCategory.builder().name("Cầu lông").iconUrl("https://img.icons8.com/color/96/badminton.png").active(true).build());
        SportCategory bongda = categoryRepository.save(SportCategory.builder().name("Bóng đá").iconUrl("https://img.icons8.com/color/96/football.png").active(true).build());
        SportCategory tennis = categoryRepository.save(SportCategory.builder().name("Tennis").iconUrl("https://img.icons8.com/color/96/tennis.png").active(true).build());
        SportCategory bongchuyen = categoryRepository.save(SportCategory.builder().name("Bóng chuyền").iconUrl("https://img.icons8.com/color/96/volleyball.png").active(true).build());
        SportCategory bongro = categoryRepository.save(SportCategory.builder().name("Bóng rổ").iconUrl("https://img.icons8.com/color/96/basketball.png").active(true).build());

        // Venues
        Venue venue1 = venueRepository.save(Venue.builder()
                .name("Pickleball FLC")
                .address("Tầng T, Toà HH4 FLC Garden City, xã...")
                .phone("0901234567")
                .imageUrl("https://images.unsplash.com/photo-1554068865-24cecd4e34b8?w=800")
                .openTime(LocalTime.of(5, 0))
                .closeTime(LocalTime.of(23, 0))
                .rating(5.0).ratingCount(42)
                .pricePerSlot(150000.0)
                .category(pickleball)
                .owner(owner)
                .active(true)
                .build());

        Venue venue2 = venueRepository.save(Venue.builder()
                .name("Pickleball B32")
                .address("Khu Đô Thị Chức Năng Đại Mỗ, P. Đại...")
                .phone("0909876543")
                .imageUrl("https://images.unsplash.com/photo-1622279457486-62dcc4a431d6?w=800")
                .openTime(LocalTime.of(4, 30))
                .closeTime(LocalTime.of(23, 0))
                .rating(5.0).ratingCount(28)
                .pricePerSlot(120000.0)
                .category(pickleball)
                .owner(owner)
                .active(true)
                .build());

        Venue venue3 = venueRepository.save(Venue.builder()
                .name("Sân cầu lông Thành Công")
                .address("12 Nguyễn Công Hoan, Ba Đình, Hà Nội")
                .phone("0911223344")
                .imageUrl("https://images.unsplash.com/photo-1626224583764-f87db24ac4ea?w=800")
                .openTime(LocalTime.of(6, 0))
                .closeTime(LocalTime.of(22, 0))
                .rating(4.8).ratingCount(56)
                .pricePerSlot(100000.0)
                .category(caulong)
                .owner(owner)
                .active(true)
                .build());

        Venue venue4 = venueRepository.save(Venue.builder()
                .name("Sân bóng đá mini Mỹ Đình")
                .address("Khu Liên hợp Thể thao Mỹ Đình")
                .phone("0922334455")
                .imageUrl("https://images.unsplash.com/photo-1575361204480-aadea25e6e68?w=800")
                .openTime(LocalTime.of(7, 0))
                .closeTime(LocalTime.of(22, 0))
                .rating(4.5).ratingCount(120)
                .pricePerSlot(500000.0)
                .category(bongda)
                .owner(owner)
                .active(true)
                .build());

        // Courts for venue1
        List<Court> courts1 = List.of(
                Court.builder().name("Sân 1").venue(venue1).active(true).build(),
                Court.builder().name("Sân 2").venue(venue1).active(true).build(),
                Court.builder().name("Sân 3").venue(venue1).active(true).build(),
                Court.builder().name("Sân 4").venue(venue1).active(true).build()
        );
        courtRepository.saveAll(courts1);

        // Courts for venue2
        courtRepository.saveAll(List.of(
                Court.builder().name("Sân 1").venue(venue2).active(true).build(),
                Court.builder().name("Sân 2").venue(venue2).active(true).build(),
                Court.builder().name("Sân 3").venue(venue2).active(true).build()
        ));

        // Courts for venue3
        courtRepository.saveAll(List.of(
                Court.builder().name("Sân 1").venue(venue3).active(true).build(),
                Court.builder().name("Sân 2").venue(venue3).active(true).build()
        ));

        // Courts for venue4
        courtRepository.saveAll(List.of(
                Court.builder().name("Sân A").venue(venue4).active(true).build(),
                Court.builder().name("Sân B").venue(venue4).active(true).build()
        ));

        // Generate time slots for today (venue1)
        LocalDate today = LocalDate.now();
        for (Court court : courts1) {
            for (int hour = 5; hour < 23; hour++) {
                for (int min = 0; min < 60; min += 30) {
                    TimeSlot.SlotStatus status = TimeSlot.SlotStatus.AVAILABLE;
                    // Mock some booked/locked slots
                    if (hour >= 17 && hour <= 19 && court.getName().equals("Sân 2")) {
                        status = TimeSlot.SlotStatus.BOOKED;
                    }
                    if (hour >= 18 && hour <= 20 && court.getName().equals("Sân 3")) {
                        status = TimeSlot.SlotStatus.BOOKED;
                    }
                    if (hour >= 20 && court.getName().equals("Sân 4")) {
                        status = TimeSlot.SlotStatus.LOCKED;
                    }

                    timeSlotRepository.save(TimeSlot.builder()
                            .court(court)
                            .date(today)
                            .startTime(LocalTime.of(hour, min))
                            .endTime(LocalTime.of(hour, min).plusMinutes(30))
                            .status(status)
                            .build());
                }
            }
        }

        System.out.println("=== Seed data loaded successfully ===");
    }
}
