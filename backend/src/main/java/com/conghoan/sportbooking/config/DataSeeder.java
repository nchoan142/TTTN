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
        User admin = userRepository.save(User.builder()
                .fullName("Admin")
                .email("admin@email.com")
                .password(passwordEncoder.encode("admin123"))
//                .password("123456")
                .phone("0900000000")
                .role(User.Role.ADMIN)
                .build());

        genData();
        System.out.println("=== Đã tạo dữ liệu ===");
    }

    private void genData() {
        SportCategory pickleball = categoryRepository.save(SportCategory.builder().name("Pickleball").iconUrl("https://cdn-icons-png.flaticon.com/512/12557/12557968.png").active(true).build());
        SportCategory caulong = categoryRepository.save(SportCategory.builder().name("Cầu lông").iconUrl("https://img.icons8.com/color/96/badminton.png").active(true).build());
        SportCategory bongda = categoryRepository.save(SportCategory.builder().name("Bóng đá").iconUrl("https://img.icons8.com/color/96/football.png").active(true).build());
        SportCategory tennis = categoryRepository.save(SportCategory.builder().name("Tennis").iconUrl("https://img.icons8.com/color/96/tennis.png").active(true).build());
        SportCategory bongchuyen = categoryRepository.save(SportCategory.builder().name("Bóng chuyền").iconUrl("https://img.icons8.com/color/96/volleyball.png").active(true).build());
        SportCategory bongro = categoryRepository.save(SportCategory.builder().name("Bóng rổ").iconUrl("https://img.icons8.com/color/96/basketball.png").active(true).build());

        Venue venue1 = venueRepository.save(Venue.builder()
                .name("Pickleball FLC")
                .address("Tầng T, Toà HH4 FLC Garden City, Đại Mỗ, Hà Nội")
                .phone("0988172838")
                .imageUrl("https://img.magnific.com/premium-photo/3d-illustration-net-indoor-pickleball-court-with-blue-green-colors-sports-complex-area_747516-507.jpg")
                .openTime(LocalTime.of(6, 0))
                .closeTime(LocalTime.of(23, 0))
                .rating(5.0).ratingCount(42)
                .pricePerSlot(150000.0)
                .category(pickleball)
                .active(true)
                .build());

        Venue venue2 = venueRepository.save(Venue.builder()
                .name("Pickleball B32")
                .address("Chung cư B32, Sân Pickleball, Đ. Đại Mỗ Tổ 13, Phường, Đại Mỗ, Hà Nội")
                .phone("0936017689")
                .imageUrl("https://lh3.googleusercontent.com/gps-cs-s/APNQkAHcvjxgbythWP_vKE5wkQfq7vePvWOO4PZrRF3up7v2kUqsI0b1nd8TiDKThfQzDgmmwRa425DStFr589qTvnZK4rYVfJsexqK4SyDqWvoX4Ar5KumIHgd1VAeDVmUfErCTZuGK8lJqmfs=s1360-w1360-h1020-rw")
                .openTime(LocalTime.of(6, 30))
                .closeTime(LocalTime.of(23, 0))
                .rating(5.0).ratingCount(28)
                .pricePerSlot(120000.0)
                .category(pickleball)
                .active(true)
                .build());

        Venue venue3 = venueRepository.save(Venue.builder()
                .name("Pickleball Hoàng Minh")
                .address("34-38 Lê Anh Xuân, Bến Thành, Hồ Chí Minh")
                .phone("0948742222")
                .imageUrl("https://lh3.googleusercontent.com/gps-cs-s/APNQkAGjjDGMpEh0gT5ScDA9wQbBekIBCEd9j6UakAmCHsVh0DVyzu6_2bLIEy2C2HjcOOYjRMILSJNTNTv5Kgh3VaWpMfqtzT8okwNpZM9rLovLSOXqAKaKEpvkH2rzBuXMXXdon8iJ=s1360-w1360-h1020-rw")
                .openTime(LocalTime.of(6, 0))
                .closeTime(LocalTime.of(22, 0))
                .rating(4.8).ratingCount(56)
                .pricePerSlot(250000.0)
                .category(pickleball)
                .active(true)
                .build());

        Venue venue4 = venueRepository.save(Venue.builder()
                .name("ATUS Badminton")
                .address("52 Đ. Hữu Hưng, Tây Mỗ, Hà Nội")
                .phone("0988172838")
                .imageUrl("https://lh3.googleusercontent.com/gps-cs-s/APNQkAGo0GKNlB1Mg5L_r0GS3LA1d0U9anwoJZszdLOXXr6gICjMZ7E3Ogby-ea95ofUlsJHpA54WMv2Od_lSiCsdfk3TWItzirp9B-jWsLt38uNJ2lqI4GzBWO9CSLoAzm_jICLx7I=s1360-w1360-h1020-rw")
                .openTime(LocalTime.of(6, 0))
                .closeTime(LocalTime.of(23, 30))
                .rating(4.5).ratingCount(6)
                .pricePerSlot(110000.0)
                .category(caulong)
                .active(true)
                .build());

        Venue venue5 = venueRepository.save(Venue.builder()
                .name("Sân Cầu Lông LITA")
                .address("442 Lê Hồng Phong, Nam Nha Trang, Khánh Hòa")
                .phone("0708660670")
                .imageUrl("https://lh3.googleusercontent.com/gps-cs-s/APNQkAGhB14ek-LJtA1GkZdZH0fUMb5A7srWd-N-QjqXCGWXfRltrboKZnINCB5DJpHAhI7IPFs6AVkKiXCefXh5upvpnqWL_RzXdcBDWMGvgcSzm39IQX8pwSxknzJ4v8-AvmzZ3ZJtUg=s1360-w1360-h1020-rw")
                .openTime(LocalTime.of(6, 0))
                .closeTime(LocalTime.of(23, 30))
                .rating(0.0).ratingCount(0)
                .pricePerSlot(110000.0)
                .category(caulong)
                .active(true)
                .build());

        Venue venue6 = venueRepository.save(Venue.builder()
                .name("Sân cầu lông SWIN")
                .address("Ngõ 214 Đường Nguyễn Xiển, Xã Tân Triều, Huyện Thanh Trì, Hà Nội")
                .phone("0764321666")
                .imageUrl("https://img.olympics.com/images/image/private/t_s_pog_staticContent_hero_xl_2x/f_auto/primary/kfsyzuaoipfhm4qonqci")
                .openTime(LocalTime.of(6, 0))
                .closeTime(LocalTime.of(23, 30))
                .rating(0.0).ratingCount(0)
                .pricePerSlot(115000.0)
                .category(caulong)
                .active(true)
                .build());

        Venue venue7 = venueRepository.save(Venue.builder()
                .name("Sân bóng đá Bà Rịa")
                .address("Tất Thành, Bà Rịa, Hồ Chí Minh")
                .phone("0979777733")
                .imageUrl("https://nld.mediacdn.vn/zoom/594_371/291774122806476800/2021/10/28/2187554129993465108350763227094096821848166n-copy-16354152762111357065343.jpg")
                .openTime(LocalTime.of(6, 0))
                .closeTime(LocalTime.of(23, 30))
                .rating(4.9).ratingCount(500)
                .pricePerSlot(1500000.0)
                .category(bongda)
                .active(true)
                .build());

        Venue venue8 = venueRepository.save(Venue.builder()
                .name("Sân Tennis Đại Học Sư Phạm")
                .address("Xuân Thủy, Cầu Giấy, Hà Nội")
                .phone("0987459328")
                .imageUrl("https://lh3.googleusercontent.com/gps-cs-s/APNQkAGJlicPRKBqdPDsZg9zpjcQDSlFcfQp3w7RVAqkoWXLIsLv6HiQHvWxGarDobcIpp4n_xjHqOYTL3KU7-0oQOk2eFA5MTna89ioxSnqhaS6R9cHcBlSkE4VMK0SQI_TojGquqeU=s1360-w1360-h1020-rw")
                .openTime(LocalTime.of(5, 0))
                .closeTime(LocalTime.of(23, 30))
                .rating(4.9).ratingCount(6)
                .pricePerSlot(250000.0)
                .category(tennis)
                .active(true)
                .build());

        Venue venue9 = venueRepository.save(Venue.builder()
                .name("Sân bóng chuyền Bách Khoa")
                .address("42 Tạ Quang Bửu, Bách Khoa, Hai Bà Trưng, Hà Nội")
                .phone("0987459328")
                .imageUrl("https://thethaothientruong.vn/wp-content/uploads/2025/07/san-danh-bong-chuyen-ha-noi-3.jpg")
                .openTime(LocalTime.of(5, 0))
                .closeTime(LocalTime.of(23, 30))
                .rating(4.9).ratingCount(15)
                .pricePerSlot(220000.0)
                .category(bongchuyen)
                .active(true)
                .build());

        Venue venue10 = venueRepository.save(Venue.builder()
                .name("Sân bóng rổ HBA")
                .address("28 Ngô Thời Nhiệm, Xuân Hòa, Hồ Chí Minh")
                .phone("0932798996")
                .imageUrl("https://tienvinhsports.com.vn/files/sanpham/96/1/jpg/bao-gia-va-ket-cau-cac-loai-tham-bong-ro.jpg")
                .openTime(LocalTime.of(5, 0))
                .closeTime(LocalTime.of(23, 30))
                .rating(4.5).ratingCount(20)
                .pricePerSlot(270000.0)
                .category(bongro)
                .active(true)
                .build());

        courtRepository.saveAll(List.of(
                Court.builder().name("Sân 1").venue(venue1).active(true).build(),
                Court.builder().name("Sân 2").venue(venue1).active(true).build(),
                Court.builder().name("Sân 3").venue(venue1).active(true).build()
        ));
        courtRepository.saveAll(List.of(
                Court.builder().name("Sân 1").venue(venue2).active(true).build(),
                Court.builder().name("Sân 2").venue(venue2).active(true).build(),
                Court.builder().name("Sân 3").venue(venue2).active(true).build()
        ));
        courtRepository.saveAll(List.of(
                Court.builder().name("Sân 1").venue(venue3).active(true).build(),
                Court.builder().name("Sân 2").venue(venue3).active(true).build(),
                Court.builder().name("Sân 3").venue(venue3).active(true).build()
        ));
        courtRepository.saveAll(List.of(
                Court.builder().name("Sân 1").venue(venue4).active(true).build(),
                Court.builder().name("Sân 2").venue(venue4).active(true).build(),
                Court.builder().name("Sân 3").venue(venue4).active(true).build(),
                Court.builder().name("Sân 4").venue(venue4).active(true).build(),
                Court.builder().name("Sân 5").venue(venue4).active(true).build(),
                Court.builder().name("Sân 6").venue(venue4).active(true).build()
        ));
        courtRepository.saveAll(List.of(
                Court.builder().name("Sân 1").venue(venue5).active(true).build(),
                Court.builder().name("Sân 2").venue(venue5).active(true).build(),
                Court.builder().name("Sân 3").venue(venue5).active(true).build()
        ));
        courtRepository.saveAll(List.of(
                Court.builder().name("Sân 1").venue(venue6).active(true).build(),
                Court.builder().name("Sân 2").venue(venue6).active(true).build(),
                Court.builder().name("Sân 3").venue(venue6).active(true).build()
        ));
        courtRepository.saveAll(List.of(
                Court.builder().name("Sân 1").venue(venue7).active(true).build(),
                Court.builder().name("Sân 2").venue(venue7).active(true).build(),
                Court.builder().name("Sân 3").venue(venue7).active(true).build()
        ));
        courtRepository.saveAll(List.of(
                Court.builder().name("Sân 1").venue(venue8).active(true).build(),
                Court.builder().name("Sân 2").venue(venue8).active(true).build(),
                Court.builder().name("Sân 3").venue(venue8).active(true).build()
        ));
        courtRepository.saveAll(List.of(
                Court.builder().name("Sân 1").venue(venue9).active(true).build(),
                Court.builder().name("Sân 2").venue(venue9).active(true).build(),
                Court.builder().name("Sân 3").venue(venue9).active(true).build()
        ));
        courtRepository.saveAll(List.of(
                Court.builder().name("Sân 1").venue(venue10).active(true).build(),
                Court.builder().name("Sân 2").venue(venue10).active(true).build(),
                Court.builder().name("Sân 3").venue(venue10).active(true).build()
        ));
    }

    private void genData1() {
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
        User admin = userRepository.save(User.builder()
                .fullName("Admin")
                .email("admin@email.com")
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
    }
}
