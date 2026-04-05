package com.wellness.ritmo.domain.model;

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
        name = "activities",
        indexes = {
                @Index(name = "idx_activity_user_started_at", columnList = "user_id, started_at")
        }
)
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_activity_user")
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "goal_id",
            foreignKey = @ForeignKey(name = "fk_activity_goal")
    )
    private Goal goal;

    @Column(name = "distance_km", precision = 6, scale = 3)
    private BigDecimal distanceKm;

    @Column(name = "duration_sec")
    private Integer durationSec;

    @Column(name = "pace_avg_sec")
    private Integer paceAvgSec;

    @Column(name = "heart_rate_avg")
    private Integer heartRateAvg;

    @Column(name = "heart_rate_max")
    private Integer heartRateMax;

    @Column(name = "perceived_effort")
    private Integer perceivedEffort;

    @Column(name = "started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
