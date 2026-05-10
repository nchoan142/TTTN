CREATE DATABASE  IF NOT EXISTS `sport_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `sport_db`;
-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: sport_db
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `bookings`
--

DROP TABLE IF EXISTS `bookings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bookings` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `booking_date` date NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `end_time` time(6) NOT NULL,
  `note` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `start_time` time(6) NOT NULL,
  `status` enum('PENDING','CONFIRMED','COMPLETED','CANCELLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `total_price` double DEFAULT NULL,
  `court_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKrc2ar1jl63nymaipwibvab7q0` (`court_id`),
  KEY `FKeyog2oic85xg7hsu2je2lx3s6` (`user_id`),
  CONSTRAINT `FKeyog2oic85xg7hsu2je2lx3s6` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKrc2ar1jl63nymaipwibvab7q0` FOREIGN KEY (`court_id`) REFERENCES `courts` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bookings`
--

LOCK TABLES `bookings` WRITE;
/*!40000 ALTER TABLE `bookings` DISABLE KEYS */;
INSERT INTO `bookings` VALUES (1,'2026-05-03','2026-05-03 08:09:28.939878','15:30:00.000000',NULL,'15:00:00.000000','CONFIRMED',110000,13,2),(2,'2026-05-03','2026-05-03 08:09:28.939878','16:00:00.000000',NULL,'15:30:00.000000','CONFIRMED',110000,13,2),(3,'2026-05-03','2026-05-03 08:09:28.939878','17:00:00.000000',NULL,'16:30:00.000000','PENDING',110000,13,2),(4,'2026-05-03','2026-05-03 08:09:28.939878','16:30:00.000000',NULL,'16:00:00.000000','PENDING',110000,13,2),(5,'2026-05-03','2026-05-03 10:13:16.092945','07:00:00.000000',NULL,'06:30:00.000000','PENDING',150000,1,2),(6,'2026-05-03','2026-05-03 10:13:16.092945','08:30:00.000000',NULL,'08:00:00.000000','PENDING',150000,1,2),(7,'2026-05-03','2026-05-03 10:13:16.092945','08:00:00.000000',NULL,'07:30:00.000000','PENDING',150000,1,2),(8,'2026-05-03','2026-05-03 10:13:16.092945','06:30:00.000000',NULL,'06:00:00.000000','PENDING',150000,1,2),(9,'2026-05-03','2026-05-03 10:13:16.092945','07:30:00.000000',NULL,'07:00:00.000000','CANCELLED',150000,1,2),(10,'2026-05-03','2026-05-03 10:13:16.569292','09:00:00.000000',NULL,'08:30:00.000000','CANCELLED',150000,1,2);
/*!40000 ALTER TABLE `bookings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `courts`
--

DROP TABLE IF EXISTS `courts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `courts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `venue_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKc588eogjvkwv2gfs9ulycwbk1` (`venue_id`),
  CONSTRAINT `FKc588eogjvkwv2gfs9ulycwbk1` FOREIGN KEY (`venue_id`) REFERENCES `venues` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `courts`
--

LOCK TABLES `courts` WRITE;
/*!40000 ALTER TABLE `courts` DISABLE KEYS */;
INSERT INTO `courts` VALUES (1,_binary '','Sân 1',1),(2,_binary '','Sân 2',1),(3,_binary '','Sân 3',1),(4,_binary '','Sân 1',2),(5,_binary '','Sân 2',2),(6,_binary '','Sân 3',2),(7,_binary '','Sân 1',3),(8,_binary '','Sân 2',3),(9,_binary '','Sân 3',3),(10,_binary '','Sân 1',4),(11,_binary '','Sân 2',4),(12,_binary '','Sân 3',4),(13,_binary '','Sân 4',4),(14,_binary '','Sân 5',4),(15,_binary '','Sân 6',4),(16,_binary '','Sân 1',5),(17,_binary '','Sân 2',5),(18,_binary '','Sân 3',5),(19,_binary '','Sân 1',6),(20,_binary '','Sân 2',6),(21,_binary '','Sân 3',6),(22,_binary '','Sân 1',7),(23,_binary '','Sân 2',7),(24,_binary '','Sân 3',7),(25,_binary '','Sân 1',8),(26,_binary '','Sân 2',8),(27,_binary '','Sân 3',8),(28,_binary '','Sân 1',9),(29,_binary '','Sân 2',9),(30,_binary '','Sân 3',9),(31,_binary '','Sân 1',10),(32,_binary '','Sân 2',10),(33,_binary '','Sân 3',10);
/*!40000 ALTER TABLE `courts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reviews`
--

DROP TABLE IF EXISTS `reviews`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reviews` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `comment` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `rating` int DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `venue_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKcgy7qjc1r99dp117y9en6lxye` (`user_id`),
  KEY `FKj3q0yctu4l1j9m0vj1ddj9y3s` (`venue_id`),
  CONSTRAINT `FKcgy7qjc1r99dp117y9en6lxye` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKj3q0yctu4l1j9m0vj1ddj9y3s` FOREIGN KEY (`venue_id`) REFERENCES `venues` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reviews`
--

LOCK TABLES `reviews` WRITE;
/*!40000 ALTER TABLE `reviews` DISABLE KEYS */;
INSERT INTO `reviews` VALUES (1,'dich vu tot','2026-05-03 08:14:00.302581',5,3,4),(2,'dich vu te','2026-05-03 08:14:23.462829',1,3,4);
/*!40000 ALTER TABLE `reviews` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sport_categories`
--

DROP TABLE IF EXISTS `sport_categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sport_categories` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `icon_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_ng1opy6ci38r3asqhojfw5lgf` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sport_categories`
--

LOCK TABLES `sport_categories` WRITE;
/*!40000 ALTER TABLE `sport_categories` DISABLE KEYS */;
INSERT INTO `sport_categories` VALUES (1,_binary '','https://cdn-icons-png.flaticon.com/512/12557/12557968.png','Pickleball'),(2,_binary '','https://img.icons8.com/color/96/badminton.png','Cầu lông'),(3,_binary '','https://img.icons8.com/color/96/football.png','Bóng đá'),(4,_binary '','https://img.icons8.com/color/96/tennis.png','Tennis'),(5,_binary '','https://img.icons8.com/color/96/volleyball.png','Bóng chuyền'),(6,_binary '','https://img.icons8.com/color/96/basketball.png','Bóng rổ');
/*!40000 ALTER TABLE `sport_categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `time_slots`
--

DROP TABLE IF EXISTS `time_slots`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `time_slots` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `date` date NOT NULL,
  `end_time` time(6) NOT NULL,
  `start_time` time(6) NOT NULL,
  `status` enum('AVAILABLE','PENDING','BOOKED','LOCKED','EVENT') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `booking_id` bigint DEFAULT NULL,
  `court_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKh1sra14dvfa3ncl6frti4cdq1` (`booking_id`),
  KEY `FKo5mxrgkshctd8xcd9r946ldkc` (`court_id`),
  CONSTRAINT `FKh1sra14dvfa3ncl6frti4cdq1` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`),
  CONSTRAINT `FKo5mxrgkshctd8xcd9r946ldkc` FOREIGN KEY (`court_id`) REFERENCES `courts` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `time_slots`
--

LOCK TABLES `time_slots` WRITE;
/*!40000 ALTER TABLE `time_slots` DISABLE KEYS */;
/*!40000 ALTER TABLE `time_slots` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `full_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `role` enum('USER','OWNER','ADMIN') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_6dotkott2kjsp8vw4d0m25fb7` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,NULL,'2026-05-03 08:07:41.748595','admin@email.com','Admin','$2a$10$8uGxrGKKMuC/pRPGZnk4vuDznw4axvz0a/p7qbr3iqWYFRW.958Ue','0900000000','ADMIN'),(2,NULL,'2026-05-03 08:08:50.584016','nchoan142@gmail.com','Nguyễn Công Hoàn','$2a$10$0l7gCma1MIVqn/rp9NcE2ODSIcqjKw4mQHFJlu1k7ntSZqw8Rhdgy','0964930762','USER'),(3,NULL,'2026-05-03 08:13:29.116934','lklinh@gmail.com','Lương Khánh Linh','$2a$10$zB0Yp6P.K54tda3sSpQHWOPXM9bnaJqkP9.cr8vxiQjF64fza5xla','0968686899','USER');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `venues`
--

DROP TABLE IF EXISTS `venues`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `venues` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `close_time` time(6) DEFAULT NULL,
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `open_time` time(6) DEFAULT NULL,
  `phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `price_per_slot` double DEFAULT NULL,
  `rating` double DEFAULT NULL,
  `rating_count` int DEFAULT NULL,
  `category_id` bigint DEFAULT NULL,
  `owner_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKka0ka9dkc163o98sr14gy069u` (`category_id`),
  KEY `FKq22to1hxop8unjpmeawieega6` (`owner_id`),
  CONSTRAINT `FKka0ka9dkc163o98sr14gy069u` FOREIGN KEY (`category_id`) REFERENCES `sport_categories` (`id`),
  CONSTRAINT `FKq22to1hxop8unjpmeawieega6` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `venues`
--

LOCK TABLES `venues` WRITE;
/*!40000 ALTER TABLE `venues` DISABLE KEYS */;
INSERT INTO `venues` VALUES (1,_binary '','Tầng T, Toà HH4 FLC Garden City, Đại Mỗ, Hà Nội','23:00:00.000000','https://img.magnific.com/premium-photo/3d-illustration-net-indoor-pickleball-court-with-blue-green-colors-sports-complex-area_747516-507.jpg','Pickleball FLC','06:00:00.000000','0988172838',150000,5,42,1,NULL),(2,_binary '','Chung cư B32, Sân Pickleball, Đ. Đại Mỗ Tổ 13, Phường, Đại Mỗ, Hà Nội','23:00:00.000000','https://lh3.googleusercontent.com/gps-cs-s/APNQkAHcvjxgbythWP_vKE5wkQfq7vePvWOO4PZrRF3up7v2kUqsI0b1nd8TiDKThfQzDgmmwRa425DStFr589qTvnZK4rYVfJsexqK4SyDqWvoX4Ar5KumIHgd1VAeDVmUfErCTZuGK8lJqmfs=s1360-w1360-h1020-rw','Pickleball B32','06:30:00.000000','0936017689',120000,5,28,1,NULL),(3,_binary '','34-38 Lê Anh Xuân, Bến Thành, Hồ Chí Minh','22:00:00.000000','https://lh3.googleusercontent.com/gps-cs-s/APNQkAGjjDGMpEh0gT5ScDA9wQbBekIBCEd9j6UakAmCHsVh0DVyzu6_2bLIEy2C2HjcOOYjRMILSJNTNTv5Kgh3VaWpMfqtzT8okwNpZM9rLovLSOXqAKaKEpvkH2rzBuXMXXdon8iJ=s1360-w1360-h1020-rw','Pickleball Hoàng Minh','06:00:00.000000','0948742222',250000,4.8,56,1,NULL),(4,_binary '','52 Đ. Hữu Hưng, Tây Mỗ, Hà Nội','23:30:00.000000','https://lh3.googleusercontent.com/gps-cs-s/APNQkAGo0GKNlB1Mg5L_r0GS3LA1d0U9anwoJZszdLOXXr6gICjMZ7E3Ogby-ea95ofUlsJHpA54WMv2Od_lSiCsdfk3TWItzirp9B-jWsLt38uNJ2lqI4GzBWO9CSLoAzm_jICLx7I=s1360-w1360-h1020-rw','ATUS Badminton','06:00:00.000000','0988172838',110000,3,2,2,NULL),(5,_binary '','442 Lê Hồng Phong, Nam Nha Trang, Khánh Hòa','23:30:00.000000','https://lh3.googleusercontent.com/gps-cs-s/APNQkAGhB14ek-LJtA1GkZdZH0fUMb5A7srWd-N-QjqXCGWXfRltrboKZnINCB5DJpHAhI7IPFs6AVkKiXCefXh5upvpnqWL_RzXdcBDWMGvgcSzm39IQX8pwSxknzJ4v8-AvmzZ3ZJtUg=s1360-w1360-h1020-rw','Sân Cầu Lông LITA','06:00:00.000000','0708660670',110000,0,0,2,NULL),(6,_binary '','Ngõ 214 Đường Nguyễn Xiển, Xã Tân Triều, Huyện Thanh Trì, Hà Nội','23:30:00.000000','https://img.olympics.com/images/image/private/t_s_pog_staticContent_hero_xl_2x/f_auto/primary/kfsyzuaoipfhm4qonqci','Sân cầu lông SWIN','06:00:00.000000','0764321666',115000,0,0,2,NULL),(7,_binary '','Tất Thành, Bà Rịa, Hồ Chí Minh','23:30:00.000000','https://nld.mediacdn.vn/zoom/594_371/291774122806476800/2021/10/28/2187554129993465108350763227094096821848166n-copy-16354152762111357065343.jpg','Sân bóng đá Bà Rịa','06:00:00.000000','0979777733',1500000,4.9,500,3,NULL),(8,_binary '','Xuân Thủy, Cầu Giấy, Hà Nội','23:30:00.000000','https://lh3.googleusercontent.com/gps-cs-s/APNQkAGJlicPRKBqdPDsZg9zpjcQDSlFcfQp3w7RVAqkoWXLIsLv6HiQHvWxGarDobcIpp4n_xjHqOYTL3KU7-0oQOk2eFA5MTna89ioxSnqhaS6R9cHcBlSkE4VMK0SQI_TojGquqeU=s1360-w1360-h1020-rw','Sân Tennis Đại Học Sư Phạm','05:00:00.000000','0987459328',250000,4.9,6,4,NULL),(9,_binary '','42 Tạ Quang Bửu, Bách Khoa, Hai Bà Trưng, Hà Nội','23:30:00.000000','https://thethaothientruong.vn/wp-content/uploads/2025/07/san-danh-bong-chuyen-ha-noi-3.jpg','Sân bóng chuyền Bách Khoa','05:00:00.000000','0987459328',220000,4.9,15,5,NULL),(10,_binary '','28 Ngô Thời Nhiệm, Xuân Hòa, Hồ Chí Minh','23:30:00.000000','https://tienvinhsports.com.vn/files/sanpham/96/1/jpg/bao-gia-va-ket-cau-cac-loai-tham-bong-ro.jpg','Sân bóng rổ HBA','05:00:00.000000','0932798996',270000,4.5,20,6,NULL);
/*!40000 ALTER TABLE `venues` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-10 20:55:41
