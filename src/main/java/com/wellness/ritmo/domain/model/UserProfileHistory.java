package com.wellness.ritmo.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "user_profile_history",
        indexes = {
                @Index(name = "idx_profile_history_user_recorded", columnList = "user_id, recorded_at")
        }
)
public class UserProfileHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_profile_history_user")
    )
    private User user;

    @Column(name = "weight_kg")
    private Double weightKg;

    @Column(name = "pace_avg_seg")
    private Integer paceAvgSeg;

    @Enumerated(EnumType.STRING)
    @Column(name = "conditioning_lvl", length = 30)
    private ConditioningLevel conditioningLevel;

    @Column(name = "recorded_at", nullable = false, updatable = false)
    private LocalDateTime recordedAt;

    @PrePersist
    private void onRecord() {
        this.recordedAt = LocalDateTime.now();
    }
}
