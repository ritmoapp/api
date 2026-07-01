package com.wellness.ritmo.application.usecase.goal;

import com.wellness.ritmo.api.dto.GoalResponseDto;
import com.wellness.ritmo.api.dto.mapper.GoalMapper;
import com.wellness.ritmo.application.service.AuthenticatedUserService;
import com.wellness.ritmo.domain.model.Goal;
import com.wellness.ritmo.domain.service.GoalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Use Case: Recuperar objetivo por ID
 * 
 * Responsabilidade: Orquestrar a recuperação de um objetivo específico.
 * 
 * SEGURANÇA:
 * ==========
 * ✓ Valida que o usuário autenticado é o proprietário do objetivo
 * ✓ Impede acesso cruzado (XAC): um usuário não pode ler goals de outro
 * 
 * @author Arquitetura Ritmo
 * @see GoalService
 * @see GoalResponseDto
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class GetGoalUseCase {

    private final GoalService goalService;
    private final AuthenticatedUserService authenticatedUserService;

    /**
     * Executa o caso de uso de recuperação de objetivo.
     * 
     * @param userId ID do usuário proprietário do objetivo
     * @param goalId ID do objetivo a ser recuperado
     * @return GoalResponseDto com os dados do objetivo
     * 
     * @throws org.springframework.security.access.AccessDeniedException 
     *         se o usuário autenticado não é o proprietário
     * @throws jakarta.persistence.EntityNotFoundException 
     *         se o objetivo não existe
     */
    public GoalResponseDto execute(Long userId, Long goalId) {
        log.info("[GetGoalUseCase] Recuperando objetivo {} para usuário: {}", goalId, userId);

        Long authenticatedUserId = authenticatedUserService.getCurrentUserId();
        authenticatedUserService.validateOwnership(authenticatedUserId, userId);

        Goal goal = goalService.getById(userId, goalId);

        log.debug("[GetGoalUseCase] Objetivo recuperado com sucesso. GoalId: {}", goal.getId());

        return GoalMapper.toDto(goal);
    }
}
