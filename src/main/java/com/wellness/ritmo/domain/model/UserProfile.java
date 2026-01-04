package com.wellness.ritmo.domain.model;

import jakarta.persistence.*;

@Table(name="user_profile")
@Entity
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Relação 1–1 com User
     */
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
    private Integer birth_date;

    @Column(name = "height_cm")
    private Integer heightCm;

    @Column(name = "weight_kg")
    private Double weightKg;

    @Enumerated(EnumType.STRING)
    @Column(name = "conditioning_lvl", length = 30)
    private ConditioningLevel conditioningLevel;

    /**
     * Ritmo médio (ex: min/km) — pode ser Double ou Duration,
     */
    @Column(name = "pace_avg_seg")
    private Integer paceAvgSeg;
}
