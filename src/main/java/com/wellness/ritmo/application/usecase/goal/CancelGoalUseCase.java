package com.wellness.ritmo.application.usecase.goal;

import com.wellness.ritmo.application.service.AuthenticatedUserService;
import com.wellness.ritmo.domain.service.GoalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Use Case: Cancelar objetivo
 * 
 * Responsabilidade: Orquestrar o cancelamento de um objetivo.
 * 
 * REGRAS DE NEGÓCIO:
 * ==================
 * ✓ Apenas objetivos com status OPEN podem ser cancelados
 * ✗ Objetivos COMPLETED não podem ser cancelados
 * 
 * SEGURANÇA:
 * ==========
 * ✓ Valida que o usuário autenticado é o proprietário do objetivo
 * ✓ Impede acesso cruzado (XAC): um usuário não pode cancelar goals de outro
 * 
 * @author Arquitetura Ritmo
 * @see GoalService
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CancelGoalUseCase {

    private final GoalService goalService;
    private final AuthenticatedUserService authenticatedUserService;

    /**
     * Executa o caso de uso de cancelamento de objetivo.
     * 
     * @param userId ID do usuário proprietário do objetivo
     * @param goalId ID do objetivo a ser cancelado
     * 
     * @throws org.springframework.security.access.AccessDeniedException 
     *         se o usuário autenticado não é o proprietário
     * @throws jakarta.persistence.EntityNotFoundException 
     *         se o objetivo não existe
     * @throws IllegalStateException 
     *         se o objetivo já está COMPLETED
     */
    public void execute(Long userId, Long goalId) {
        log.info("[CancelGoalUseCase] Cancelando objetivo {} para usuário: {}", goalId, userId);

        Long authenticatedUserId = authenticatedUserService.getCurrentUserId();
        authenticatedUserService.validateOwnership(authenticatedUserId, userId);

        goalService.cancel(userId, goalId);

        log.info("[CancelGoalUseCase] Objetivo {} cancelado com sucesso para usuário: {}", goalId, userId);
    }
}
