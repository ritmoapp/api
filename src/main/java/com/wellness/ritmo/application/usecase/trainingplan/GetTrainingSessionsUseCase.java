package com.wellness.ritmo.application.usecase.trainingplan;

import com.wellness.ritmo.api.dto.SessionResponseDto;
import com.wellness.ritmo.api.dto.mapper.TrainingPlanMapper;
import com.wellness.ritmo.application.service.AuthenticatedUserService;
import com.wellness.ritmo.domain.model.TrainingSession;
import com.wellness.ritmo.domain.service.TrainingPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class GetTrainingSessionsUseCase {

    private final TrainingPlanService trainingPlanService;
    private final AuthenticatedUserService authenticatedUserService;

    public List<SessionResponseDto> execute(Long userId, Long planId) {
        log.info("[GetTrainingSessionsUseCase] Buscando sessões do plano: {} para usuário: {}", planId, userId);

        Long authenticatedUserId = authenticatedUserService.getCurrentUserId();
        authenticatedUserService.validateOwnership(authenticatedUserId, userId);

        List<TrainingSession> sessions = trainingPlanService.findSessions(userId, planId);
        log.info("[GetTrainingSessionsUseCase] {} sessões encontradas", sessions.size());

        return sessions.stream()
                .map(TrainingPlanMapper::toSessionResponse)
                .toList();
    }
}
