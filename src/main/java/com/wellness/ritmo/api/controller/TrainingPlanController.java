package com.wellness.ritmo.api.controller;

import com.wellness.ritmo.api.dto.GeneratePlanRequest;
import com.wellness.ritmo.api.dto.SessionResponseDto;
import com.wellness.ritmo.api.dto.TrainingPlanResponseDto;
import com.wellness.ritmo.application.service.AuthenticatedUserService;
import com.wellness.ritmo.application.usecase.trainingplan.CompleteTrainingSessionUseCase;
import com.wellness.ritmo.application.usecase.trainingplan.GenerateTrainingPlanUseCase;
import com.wellness.ritmo.application.usecase.trainingplan.GetCurrentTrainingPlanUseCase;
import com.wellness.ritmo.application.usecase.trainingplan.GetTrainingSessionsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users/{userId}/training-plans")
@RequiredArgsConstructor
@Tag(name = "Training Plans", description = "Gerenciamento de planos de treino")
public class TrainingPlanController {

    private final GenerateTrainingPlanUseCase generateTrainingPlanUseCase;
    private final GetCurrentTrainingPlanUseCase getCurrentTrainingPlanUseCase;
    private final GetTrainingSessionsUseCase getTrainingSessionsUseCase;
    private final CompleteTrainingSessionUseCase completeTrainingSessionUseCase;
    private final AuthenticatedUserService authenticatedUserService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Gera um plano de treino para o usuário")
    @ApiResponse(responseCode = "201", description = "Plano criado com sucesso")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Acesso negado: você não pode gerar planos para outro usuário")
    @ApiResponse(responseCode = "404", description = "Goal ou perfil não encontrado para o usuário")
    @ApiResponse(responseCode = "409", description = "Já existe um plano ativo para este usuário")
    public TrainingPlanResponseDto generate(
            @PathVariable Long userId,
            @Valid @RequestBody GeneratePlanRequest request
    ) {
        Long authenticatedUserId = authenticatedUserService.getCurrentUserId();
        authenticatedUserService.validateOwnership(authenticatedUserId, userId);
        
        log.info("[TrainingPlanController] Gerando plano para usuário: {}", userId);
        return generateTrainingPlanUseCase.execute(userId, request);
    }

    @GetMapping("/current")
    @Operation(summary = "Retorna o plano ativo da semana atual")
    @ApiResponse(responseCode = "200", description = "Plano ativo encontrado")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Acesso negado: você não pode acessar planos de outro usuário")
    @ApiResponse(responseCode = "404", description = "Nenhum plano ativo encontrado para o usuário")
    public TrainingPlanResponseDto current(@PathVariable Long userId) {
        Long authenticatedUserId = authenticatedUserService.getCurrentUserId();
        authenticatedUserService.validateOwnership(authenticatedUserId, userId);
        
        log.info("[TrainingPlanController] Buscando plano ativo para usuário: {}", userId);
        return getCurrentTrainingPlanUseCase.execute(userId);
    }

    @GetMapping("/{planId}/sessions")
    @Operation(summary = "Retorna as sessões de um plano de treino")
    @ApiResponse(responseCode = "200", description = "Lista de sessões retornada com sucesso")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Acesso negado: você não pode acessar sessões de outro usuário")
    @ApiResponse(responseCode = "404", description = "Plano não encontrado ou não pertence ao usuário")
    public List<SessionResponseDto> sessions(
            @PathVariable Long userId,
            @PathVariable Long planId
    ) {
        Long authenticatedUserId = authenticatedUserService.getCurrentUserId();
        authenticatedUserService.validateOwnership(authenticatedUserId, userId);
        
        log.info("[TrainingPlanController] Buscando sessões do plano: {} para usuário: {}", planId, userId);
        return getTrainingSessionsUseCase.execute(userId, planId);
    }

    @PatchMapping("/{planId}/sessions/{sessionId}/complete")
    @Operation(summary = "Marca uma sessão como concluída")
    @ApiResponse(responseCode = "200", description = "Sessão marcada como concluída")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Acesso negado: você não pode completar sessões de outro usuário")
    @ApiResponse(responseCode = "404", description = "Sessão não encontrada ou não pertence ao plano/usuário")
    @ApiResponse(responseCode = "409", description = "Sessão já está concluída")
    public SessionResponseDto completeSession(
            @PathVariable Long userId,
            @PathVariable Long planId,
            @PathVariable Long sessionId
    ) {
        Long authenticatedUserId = authenticatedUserService.getCurrentUserId();
        authenticatedUserService.validateOwnership(authenticatedUserId, userId);
        
        log.info("[TrainingPlanController] Completando sessão: {} do plano: {} para usuário: {}", 
                 sessionId, planId, userId);
        return completeTrainingSessionUseCase.execute(userId, planId, sessionId);
    }
}
