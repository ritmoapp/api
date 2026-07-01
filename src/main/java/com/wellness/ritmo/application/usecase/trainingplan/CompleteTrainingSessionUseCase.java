package com.wellness.ritmo.application.usecase.trainingplan;

import com.wellness.ritmo.api.dto.SessionResponseDto;
import com.wellness.ritmo.api.dto.mapper.TrainingPlanMapper;
import com.wellness.ritmo.application.service.AuthenticatedUserService;
import com.wellness.ritmo.domain.model.TrainingSession;
import com.wellness.ritmo.domain.service.TrainingPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class CompleteTrainingSessionUseCase {

    private final TrainingPlanService trainingPlanService;
    private final AuthenticatedUserService authenticatedUserService;

    public SessionResponseDto execute(Long userId, Long planId, Long sessionId) {
        log.info("[CompleteTrainingSessionUseCase] Completando sessão: {} do plano: {} para usuário: {}", 
                 sessionId, planId, userId);

        Long authenticatedUserId = authenticatedUserService.getCurrentUserId();
        authenticatedUserService.validateOwnership(authenticatedUserId, userId);

        TrainingSession session = trainingPlanService.completeSession(userId, planId, sessionId);
        log.info("[CompleteTrainingSessionUseCase] Sessão completada com sucesso");

        return TrainingPlanMapper.toSessionResponse(session);
    }
}
