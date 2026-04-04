package com.conghoan.sportbooking.repository;

import com.conghoan.sportbooking.entity.SportCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SportCategoryRepository extends JpaRepository<SportCategory, Long> {
    List<SportCategory> findByActiveTrue();
}
