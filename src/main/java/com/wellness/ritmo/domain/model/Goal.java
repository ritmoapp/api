package com.wellness.ritmo.domain.model;

import com.wellness.ritmo.domain.model.Enum.GoalStatus;
import com.wellness.ritmo.domain.model.Enum.GoalType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private GoalStatus status = GoalStatus.OPEN;

    @Column(name = "distance_km", precision = 6, scale = 3)
    private BigDecimal distanceKm;

    @Column(name = "target_time_sec")
    private Integer targetTimeSec;

    @Column(name = "pace_target_sec")
    private Integer paceTargetSec;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "race_target_id")
    private RaceTarget raceTarget;

    @Column(name = "weekly_frequency")
    private Integer weeklyFrequency;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    private void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    private void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
