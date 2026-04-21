package com.wellness.ritmo.domain.repository;

import com.wellness.ritmo.domain.model.TrainingSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrainingSessionRepository extends JpaRepository<TrainingSession, Long> {

    List<TrainingSession> findByTrainingPlanId(Long trainingPlanId);

    Optional<TrainingSession> findByIdAndTrainingPlanIdAndTrainingPlanUserId(Long id, Long planId, Long userId);
}
