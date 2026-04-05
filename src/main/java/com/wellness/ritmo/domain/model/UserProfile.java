package com.wellness.ritmo.domain.model;

import com.wellness.ritmo.domain.model.listener.UserProfileListener;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_profile")
@EntityListeners(UserProfileListener.class)
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_user_profile_user")
    )
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 20)
    private Gender gender;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "height_cm")
    private Integer heightCm;

    @Column(name = "weight_kg")
    private Double weightKg;

    @Enumerated(EnumType.STRING)
    @Column(name = "conditioning_lvl", length = 30)
    private ConditioningLevel conditioningLevel;

    @Column(name = "pace_avg_seg")
    private Integer paceAvgSeg;

    @Column(name = "weekly_mileage_km")
    private Double weeklyMileageKm;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    private void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
