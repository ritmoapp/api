package com.wellness.ritmo.domain.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "goals",
        indexes = {
                @Index(name = "idx_goal_user", columnList = "user_id")
        }
)
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Owning side
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_goal_user")
    )
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "goal_type", nullable = false, length = 50)
    private GoalType goalType;

    @Column(name = "distance_km")
    private BigDecimal distanceKm;

    // target time in seconds
    @Column(name = "target_time_sec")
    private Integer targetTimeSec;

    // pace in seconds per km
    @Column(name = "pace_target_sec")
    private Integer paceTargetSec;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // getters/setters
}
