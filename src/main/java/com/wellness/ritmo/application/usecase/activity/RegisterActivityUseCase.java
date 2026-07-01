package com.wellness.ritmo.application.usecase.activity;

import com.wellness.ritmo.api.dto.ActivityRequestDto;
import com.wellness.ritmo.api.dto.ActivityResponseDto;
import com.wellness.ritmo.api.dto.GoalEvaluationResultDto;
import com.wellness.ritmo.application.service.AuthenticatedUserService;
import com.wellness.ritmo.domain.service.ActivityService;
import com.wellness.ritmo.domain.service.ActivityService.ActivityWithEvaluation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Use Case: Registrar nova atividade (exercício)
 * 
 * Responsabilidade: Orquestrar o registro de uma atividade realizada pelo usuário.
 * Inclui avaliação contra metas se fornecida.
 * 
 * PADRÃO DE ARQUITETURA LIMPA:
 * =============================
 * 1. AUTENTICA: Obtém o ID do usuário autenticado
 * 2. AUTORIZA: Valida que o usuário autenticado é o proprietário
 * 3. EXTRAI: Campos do DTO como primitivos
 * 4. DELEGA: Passa primitivos para ActivityService
 * 5. MAPEIA: Converte entidade + avaliação para DTO de resposta
 * 6. RETORNA: ActivityResponseDto
 * 
 * FLUXO DE DADOS:
 * ===============
 * 
 *     HTTP Request (ActivityRequestDto)
 *     POST /users/123/activities
 *     { "distanceKm": 10.5, "durationSec": 3600, "goalId": 5, ... }
 *            ↓
 *   [API Layer: Controller]
 *            ↓
 *   [Application Layer: This Use Case] ← Você está aqui
 *      - Extract primitivos do DTO
 *      - Monta GoalEvaluationResultDto da avaliação
 *            ↓
 *   [Domain Layer: ActivityService]
 *      - Cria Activity (sem DTOs)
 *      - Avalia contra meta se fornecida
 *      - Retorna Activity + EvaluationResult
 *            ↓
 *   [Data Layer: Repository]
 *      - Salva Activity no banco
 *            ↓
 *     HTTP Response (ActivityResponseDto)
 * 
 * @author Arquitetura Ritmo
 * @see ActivityService
 * @see ActivityRequestDto
 * @see ActivityResponseDto
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class RegisterActivityUseCase {

    private final ActivityService activityService;
    private final AuthenticatedUserService authenticatedUserService;

    /**
     * Executa o caso de uso de registro de atividade.
     * 
     * @param userId ID do usuário que realizou a atividade
     * @param requestDto Dados da atividade vindos da requisição HTTP
     * @return ActivityResponseDto com os dados da atividade registrada
     * 
     * @throws org.springframework.security.access.AccessDeniedException 
     *         se o usuário autenticado não é o proprietário
     * @throws jakarta.persistence.EntityNotFoundException 
     *         se o usuário ou meta não existe
     * @throws IllegalArgumentException 
     *         se os campos obrigatórios estão faltando
     */
    public ActivityResponseDto execute(Long userId, ActivityRequestDto requestDto) {
        log.info("[RegisterActivityUseCase] Registrando atividade para usuário: {}", userId);

        Long authenticatedUserId = authenticatedUserService.getCurrentUserId();
        authenticatedUserService.validateOwnership(authenticatedUserId, userId);

        ActivityWithEvaluation result = activityService.register(
            userId,
            requestDto.distanceKm(),
            requestDto.durationSec(),
            requestDto.paceAvgSec(),
            requestDto.heartRateAvg(),
            requestDto.heartRateMax(),
            requestDto.perceivedEffort(),
            requestDto.startedAt(),
            requestDto.finishedAt(),
            requestDto.notes(),
            requestDto.goalId()
        );

        log.info("[RegisterActivityUseCase] Atividade registrada com sucesso. ActivityId: {}", result.activity.getId());

        GoalEvaluationResultDto evalDto = result.evaluation == null ? null
                : new GoalEvaluationResultDto(
                    result.evaluation.achieved(),
                    result.evaluation.summary(),
                    result.evaluation.delta()
                );

        return new ActivityResponseDto(
            result.activity.getId(),
            result.activity.getDistanceKm(),
            result.activity.getDurationSec(),
            result.activity.getPaceAvgSec(),
            result.activity.getStartedAt(),
            result.activity.getFinishedAt(),
            evalDto,
            "O feedback do seu treinador IA está sendo processado e será salvo em breve."
        );
    }
}
