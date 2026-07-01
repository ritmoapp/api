package com.wellness.ritmo.application.usecase.goal;

import com.wellness.ritmo.api.dto.GoalRequestDto;
import com.wellness.ritmo.api.dto.GoalResponseDto;
import com.wellness.ritmo.api.dto.mapper.GoalMapper;
import com.wellness.ritmo.application.service.AuthenticatedUserService;
import com.wellness.ritmo.domain.model.Goal;
import com.wellness.ritmo.domain.service.GoalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Use Case: Criar novo objetivo (meta)
 * 
 * Responsabilidade: Orquestrar a criação de um novo objetivo para um usuário.
 * 
 * PADRÃO DE ARQUITETURA LIMPA:
 * =============================
 * Este use case atua como intermediário entre a camada de API (DTOs) e a camada
 * de domínio (serviços puros). Ele:
 * 
 * 1. AUTENTICA: Obtém o ID do usuário autenticado
 * 2. AUTORIZA: Valida que o usuário autenticado é o proprietário do recurso
 * 3. EXTRAI: Campos individuais do DTO como primitivos
 * 4. DELEGA: Passa primitivos para GoalService (não o DTO inteiro)
 * 5. MAPEIA: Converte entidade de domínio para DTO de resposta
 * 6. RETORNA: GoalResponseDto (seguro de dados)
 * 
 * BENEFÍCIOS:
 * -----------
 * ✓ Camada de domínio permanece PURA: não conhece conceitos de HTTP (DTOs)
 * ✓ Autorização centralizada: validateOwnership() previne acesso cruzado (XAC)
 * ✓ Facilita testes: service pode ser testado com primitivos, sem Spring
 * ✓ Preparado para extensão: pode adicionar logging, eventos, auditoria
 * 
 * FLUXO DE DADOS:
 * ---------------
 * 
 *     HTTP Request (GoalRequestDto)
 *     POST /users/123/goals
 *     { "goalType": "DISTANCE", "distanceKm": 10.0, ... }
 *            ↓
 *   [API Layer: Controller]
 *      - Extrai userId do path (123)
 *      - Chama use case
 *            ↓
 *   [Application Layer: This Use Case] ← Você está aqui
 *      - getCurrentUserId() → 123 (do SecurityContext)
 *      - validateOwnership(123, 123) → ✓ OK
 *      - Extract: dto.getGoalType() → GoalType.DISTANCE
 *      - Extract: dto.getDistanceKm() → 10.0
 *            ↓
 *   [Domain Layer: GoalService]
 *      - Validação de negócio (campos obrigatórios por tipo)
 *      - Criação e persistência
 *            ↓
 *   [Data Layer: Repository]
 *      - Salva Goal no banco (usando findAllByUserId para performance)
 *            ↓
 *     HTTP Response (GoalResponseDto)
 *     201 Created
 *     { "id": 1, "goalType": "DISTANCE", "distanceKm": 10.0, ... }
 * 
 * SEGURANÇA:
 * ==========
 * ✓ validateOwnership() garante que usuário autenticado não pode criar goals para outro
 * ✓ Impede acesso cruzado (XAC - Cross-Account Access)
 * ✓ Centraliza lógica de autorização
 * 
 * EXEMPLO DE USO:
 * ---------------
 * 
 * No Controller:
 * <pre>
 * @PostMapping
 * public GoalResponseDto create(
 *     @PathVariable Long userId,
 *     @Valid @RequestBody GoalRequestDto requestDto
 * ) {
 *     authenticatedUserService.validateOwnership(
 *         authenticatedUserService.getCurrentUserId(),
 *         userId
 *     );
 *     return createGoalUseCase.execute(userId, requestDto);
 * }
 * </pre>
 * 
 * Neste Use Case:
 * <pre>
 * Goal goal = goalService.create(
 *     userId,                      // ← userId autenticado + validado
 *     dto.getGoalType(),           // ← Primitivo, não DTO
 *     dto.getDistanceKm(),         // ← Primitivo, não DTO
 *     dto.getTargetTimeSec(),      // ← Primitivo, não DTO
 *     dto.getPaceTargetSec(),      // ← Primitivo, não DTO
 *     dto.getWeeklyFrequency()     // ← Primitivo, não DTO
 * );
 * </pre>
 * 
 * @author Arquitetura Ritmo
 * @see GoalService
 * @see GoalRequestDto
 * @see GoalResponseDto
 * @see AuthenticatedUserService
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CreateGoalUseCase {

    private final GoalService goalService;
    private final AuthenticatedUserService authenticatedUserService;

    /**
     * Executa o caso de uso de criação de novo objetivo.
     * 
     * @param userId ID do usuário que será proprietário do objetivo
     * @param requestDto Dados de criação do objetivo vindos da requisição HTTP
     * @return GoalResponseDto com os dados do objetivo criado
     * 
     * @throws org.springframework.security.access.AccessDeniedException 
     *         se o usuário autenticado não é o proprietário (userId != authenticatedUserId)
     * @throws jakarta.persistence.EntityNotFoundException 
     *         se o usuário não existe
     * @throws IllegalArgumentException 
     *         se os campos obrigatórios para o tipo de objetivo estão faltando
     */
    public GoalResponseDto execute(Long userId, GoalRequestDto requestDto) {
        log.info("[CreateGoalUseCase] Iniciando criação de novo objetivo para usuário: {}", userId);

        // PASSO 1: Obter ID do usuário autenticado
        Long authenticatedUserId = authenticatedUserService.getCurrentUserId();

        // PASSO 2: Validar que o usuário autenticado é o proprietário
        // Isso previne acesso cruzado (XAC): um usuário não pode criar goals para outro
        authenticatedUserService.validateOwnership(authenticatedUserId, userId);

        // PASSO 3: Extrair campos do DTO como primitivos
        // Isso garante que a camada de domínio não dependa de DTOs
        Goal goal = goalService.create(
            userId,
            requestDto.getGoalType(),
            requestDto.getDistanceKm(),
            requestDto.getTargetTimeSec(),
            requestDto.getPaceTargetSec(),
            requestDto.getWeeklyFrequency()
        );

        log.info("[CreateGoalUseCase] Objetivo criado com sucesso. GoalId: {}", goal.getId());

        // PASSO 4: Mapear entidade de domínio para DTO de resposta
        return GoalMapper.toDto(goal);
    }
}
