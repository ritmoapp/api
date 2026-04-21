package com.wellness.ritmo.domain.repository;

import com.wellness.ritmo.domain.model.TrainingPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface TrainingPlanRepository extends JpaRepository<TrainingPlan, Long> {

    boolean existsByUserIdAndActiveTrue(Long userId);

    Optional<TrainingPlan> findByUserIdAndActiveTrueAndWeekStart(Long userId, LocalDate weekStart);

    Optional<TrainingPlan> findByIdAndUserId(Long id, Long userId);
}
