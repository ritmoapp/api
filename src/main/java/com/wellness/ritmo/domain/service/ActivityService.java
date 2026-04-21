package com.wellness.ritmo.domain.service;

import com.wellness.ritmo.api.dto.ActivityRequestDto;
import com.wellness.ritmo.api.dto.ActivityResponseDto;
import com.wellness.ritmo.api.dto.GoalEvaluationResultDto;
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
    public ActivityResponseDto register(Long userId, ActivityRequestDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: " + userId));

        Activity activity = buildActivity(dto, user);

        EvaluationResult evaluation = null;
        if (dto.goalId() != null) {
            Goal goal = goalRepository
                    .findByIdAndUserIdAndStatus(dto.goalId(), userId, GoalStatus.OPEN)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Meta não encontrada, não pertence ao usuário, ou não está OPEN: " + dto.goalId()));
            activity.setGoal(goal);
            evaluation = goalEvaluationService.evaluate(activity, goal);

            if (evaluation.achieved()) {
                goalRepository.updateStatus(goal.getId(), GoalStatus.COMPLETED);
                log.info("[Activity] Meta id={} marcada como COMPLETED para user id={}", goal.getId(), userId);
            }
        }

        Activity saved = activityRepository.save(activity);
        aiCoachService.generateFeedbackAsync(saved);

        return toResponseDto(saved, evaluation);
    }

    private Activity buildActivity(ActivityRequestDto dto, User user) {
        Activity activity = new Activity();
        activity.setUser(user);
        activity.setDistanceKm(dto.distanceKm());
        activity.setDurationSec(dto.durationSec());
        activity.setPaceAvgSec(dto.paceAvgSec());
        activity.setHeartRateAvg(dto.heartRateAvg());
        activity.setHeartRateMax(dto.heartRateMax());
        activity.setPerceivedEffort(dto.perceivedEffort());
        activity.setStartedAt(dto.startedAt());
        activity.setFinishedAt(dto.finishedAt());
        activity.setNotes(dto.notes());
        return activity;
    }

    private ActivityResponseDto toResponseDto(Activity saved, EvaluationResult evaluation) {
        GoalEvaluationResultDto evalDto = evaluation == null ? null
                : new GoalEvaluationResultDto(evaluation.achieved(), evaluation.summary(), evaluation.delta());

        return new ActivityResponseDto(
                saved.getId(),
                saved.getDistanceKm(),
                saved.getDurationSec(),
                saved.getPaceAvgSec(),
                saved.getStartedAt(),
                saved.getFinishedAt(),
                evalDto,
                "O feedback do seu treinador IA está sendo processado e será salvo em breve."
        );
    }
}
