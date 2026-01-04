package com.wellness.ritmo.domain.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "race_targets",
        indexes = {
                @Index(name = "idx_race_target_user", columnList = "user_id")
        }
)
public class RaceTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Owning side
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_race_target_user")
    )
    private User user;

    @Column(name = "race_name", nullable = false, length = 150)
    private String raceName;

    @Column(name = "race_date", nullable = false)
    private LocalDate raceDate;

    @Column(name = "race_distance_km", nullable = false)
    private BigDecimal raceDistanceKm;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;


}

