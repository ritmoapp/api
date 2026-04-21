package com.wellness.ritmo.domain.model;

import com.wellness.ritmo.domain.model.Enum.SessionStatus;
import com.wellness.ritmo.domain.model.Enum.SessionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "training_sessions")
public class TrainingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "training_plan_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_session_training_plan")
    )
    private TrainingPlan trainingPlan;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_type", nullable = false, length = 20)
    private SessionType sessionType;

    @Column(name = "target_pace_sec", nullable = false)
    private Integer targetPaceSec;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 15)
    private DayOfWeek dayOfWeek;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SessionStatus status = SessionStatus.PENDING;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
