package com.conghoan.sportbooking.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sport_categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SportCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String iconUrl;

    private boolean active = true;
}
