package com.wellness.ritmo.application.usecase.goal;

import com.wellness.ritmo.api.dto.GoalResponseDto;
import com.wellness.ritmo.api.dto.mapper.GoalMapper;
import com.wellness.ritmo.application.service.AuthenticatedUserService;
import com.wellness.ritmo.domain.model.Goal;
import com.wellness.ritmo.domain.service.GoalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Use Case: Listar objetivos do usuário
 * 
 * Responsabilidade: Orquestrar a listagem de todos os objetivos de um usuário.
 * 
 * OTIMIZAÇÃO:
 * ===========
 * ✓ Usa GoalRepository.findAllByUserId() em vez de full table scan
 * ✓ Evita carregar goals de todos os usuários e filtrar em memória
 * ✓ Preparado para pagination futura
 * 
 * SEGURANÇA:
 * ==========
 * ✓ Valida que o usuário autenticado é o proprietário
 * ✓ Impede acesso cruzado (XAC): um usuário não pode listar goals de outro
 * 
 * @author Arquitetura Ritmo
 * @see GoalService
 * @see GoalResponseDto
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ListGoalsUseCase {

    private final GoalService goalService;
    private final AuthenticatedUserService authenticatedUserService;

    /**
     * Executa o caso de uso de listagem de objetivos.
     * 
     * @param userId ID do usuário cujos objetivos serão listados
     * @return Lista de GoalResponseDto com os objetivos do usuário
     * 
     * @throws org.springframework.security.access.AccessDeniedException 
     *         se o usuário autenticado não é o proprietário
     * @throws jakarta.persistence.EntityNotFoundException 
     *         se o usuário não existe
     */
    public List<GoalResponseDto> execute(Long userId) {
        log.info("[ListGoalsUseCase] Listando objetivos para usuário: {}", userId);

        Long authenticatedUserId = authenticatedUserService.getCurrentUserId();
        authenticatedUserService.validateOwnership(authenticatedUserId, userId);

        List<Goal> goals = goalService.getAllByUser(userId);

        log.debug("[ListGoalsUseCase] {} objetivo(s) recuperado(s) para usuário: {}", 
                  goals.size(), userId);

        return goals.stream()
                .map(GoalMapper::toDto)
                .toList();
    }
}
