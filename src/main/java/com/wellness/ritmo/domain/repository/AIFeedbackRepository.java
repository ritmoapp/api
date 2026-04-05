package com.wellness.ritmo.domain.repository;

import com.wellness.ritmo.domain.model.AIFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AIFeedbackRepository extends JpaRepository<AIFeedback, Long> {
    Optional<AIFeedback> findByActivityId(Long activityId);
}
