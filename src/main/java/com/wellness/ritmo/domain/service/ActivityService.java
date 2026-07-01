package com.wellness.ritmo.domain.service;

import com.wellness.ritmo.domain.model.*;
import com.wellness.ritmo.domain.model.Enum.GoalStatus;
import com.wellness.ritmo.domain.repository.ActivityRepository;
import com.wellness.ritmo.domain.repository.GoalRepository;
import com.wellness.ritmo.domain.repository.UserRepository;
import com.wellness.ritmo.domain.service.strategy.GoalEvaluationStrategy.EvaluationResult;
import com.wellness.ritmo.infrastructure.ai.AICoachService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final GoalRepository goalRepository;
    private final UserRepository userRepository;
    private final GoalEvaluationService goalEvaluationService;
    private final AICoachService aiCoachService;

    @Transactional
    public ActivityWithEvaluation register(Long userId, BigDecimal distanceKm, Integer durationSec,
                                          Integer paceAvgSec, Integer heartRateAvg, Integer heartRateMax,
                                          Integer perceivedEffort, LocalDateTime startedAt,
                                          LocalDateTime finishedAt, String notes, Long goalId) {
        
        log.info("[ActivityService] Registrando atividade para usuário: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: " + userId));

        Activity activity = new Activity();
        activity.setUser(user);
        activity.setDistanceKm(distanceKm);
        activity.setDurationSec(durationSec);
        activity.setPaceAvgSec(paceAvgSec);
        activity.setHeartRateAvg(heartRateAvg);
        activity.setHeartRateMax(heartRateMax);
        activity.setPerceivedEffort(perceivedEffort);
        activity.setStartedAt(startedAt);
        activity.setFinishedAt(finishedAt);
        activity.setNotes(notes);

        EvaluationResult evaluation = null;
        if (goalId != null) {
            Goal goal = goalRepository
                    .findByIdAndUserIdAndStatus(goalId, userId, GoalStatus.OPEN)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Meta não encontrada, não pertence ao usuário, ou não está OPEN: " + goalId));
            activity.setGoal(goal);
            evaluation = goalEvaluationService.evaluate(activity, goal);

            if (evaluation.achieved()) {
                goalRepository.updateStatus(goal.getId(), GoalStatus.COMPLETED);
                log.info("[ActivityService] Meta id={} marcada como COMPLETED para user id={}", goal.getId(), userId);
            }
        }

        Activity saved = activityRepository.save(activity);
        aiCoachService.generateFeedbackAsync(saved);

        log.info("[ActivityService] Atividade registrada com sucesso. ActivityId: {}", saved.getId());

        return new ActivityWithEvaluation(saved, evaluation);
    }

    public static class ActivityWithEvaluation {
        public final Activity activity;
        public final EvaluationResult evaluation;

        public ActivityWithEvaluation(Activity activity, EvaluationResult evaluation) {
            this.activity = activity;
            this.evaluation = evaluation;
        }
    }
}
