package com.conghoan.sportbooking.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "venues")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String address;

    private String phone;

    private String imageUrl;

    private Double latitude;

    private Double longitude;

    private LocalTime openTime;

    private LocalTime closeTime;

    private Double rating = 5.0;

    private Integer ratingCount = 0;

    private Double pricePerSlot;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private SportCategory category;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    @JsonIgnoreProperties({"password", "createdAt"})
    private User owner;

    @OneToMany(mappedBy = "venue", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"venue"})
    private List<Court> courts;

    private boolean active = true;
}
