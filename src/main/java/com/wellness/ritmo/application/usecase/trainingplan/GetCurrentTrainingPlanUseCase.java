package com.wellness.ritmo.application.usecase.trainingplan;

import com.wellness.ritmo.api.dto.TrainingPlanResponseDto;
import com.wellness.ritmo.api.dto.mapper.TrainingPlanMapper;
import com.wellness.ritmo.application.service.AuthenticatedUserService;
import com.wellness.ritmo.domain.model.TrainingPlan;
import com.wellness.ritmo.domain.service.TrainingPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class GetCurrentTrainingPlanUseCase {

    private final TrainingPlanService trainingPlanService;
    private final AuthenticatedUserService authenticatedUserService;

    public TrainingPlanResponseDto execute(Long userId) {
        log.info("[GetCurrentTrainingPlanUseCase] Buscando plano ativo para usuário: {}", userId);

        Long authenticatedUserId = authenticatedUserService.getCurrentUserId();
        authenticatedUserService.validateOwnership(authenticatedUserId, userId);

        TrainingPlan plan = trainingPlanService.findCurrentPlan(userId);
        log.info("[GetCurrentTrainingPlanUseCase] Plano ativo encontrado para usuário: {}", userId);

        return TrainingPlanMapper.toResponse(plan);
    }
}
