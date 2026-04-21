package com.wellness.ritmo.domain.repository;

import com.wellness.ritmo.domain.model.TrainingPlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainingPlanRepository extends JpaRepository<TrainingPlan, Long> {
}
