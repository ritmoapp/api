package com.wellness.ritmo.api.controller;

import com.wellness.ritmo.api.dto.GeneratePlanRequest;
import com.wellness.ritmo.api.dto.TrainingPlanResponse;
import com.wellness.ritmo.api.dto.TrainingSessionResponse;
import com.wellness.ritmo.api.dto.mapper.TrainingPlanMapper;
import com.wellness.ritmo.domain.model.TrainingPlan;
import com.wellness.ritmo.domain.model.TrainingSession;
import com.wellness.ritmo.domain.service.TrainingPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/training-plans")
@RequiredArgsConstructor
@Tag(name = "Training Plans", description = "Gerenciamento de planos de treino")
public class TrainingPlanController {

    private final TrainingPlanService trainingPlanService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Gera um plano de treino para o usuário")
    @ApiResponse(responseCode = "201", description = "Plano criado com sucesso")
    @ApiResponse(responseCode = "404", description = "Goal ou perfil não encontrado para o usuário")
    @ApiResponse(responseCode = "409", description = "Já existe um plano ativo para este usuário")
    public TrainingPlanResponse generate(
            @PathVariable Long userId,
            @Valid @RequestBody GeneratePlanRequest request
    ) {
        TrainingPlan plan = trainingPlanService.generatePlan(userId, request.goalId());
        return TrainingPlanMapper.toResponse(plan);
    }

    @GetMapping("/current")
    @Operation(summary = "Retorna o plano ativo da semana atual")
    @ApiResponse(responseCode = "200", description = "Plano ativo encontrado")
    @ApiResponse(responseCode = "404", description = "Nenhum plano ativo encontrado para o usuário")
    public TrainingPlanResponse current(@PathVariable Long userId) {
        TrainingPlan plan = trainingPlanService.findCurrentPlan(userId);
        return TrainingPlanMapper.toResponse(plan);
    }

    @GetMapping("/{planId}/sessions")
    @Operation(summary = "Retorna as sessões de um plano de treino")
    @ApiResponse(responseCode = "200", description = "Lista de sessões retornada com sucesso")
    @ApiResponse(responseCode = "404", description = "Plano não encontrado ou não pertence ao usuário")
    public List<TrainingSessionResponse> sessions(
            @PathVariable Long userId,
            @PathVariable Long planId
    ) {
        List<TrainingSession> sessions = trainingPlanService.findSessions(userId, planId);
        return sessions.stream()
                .map(TrainingPlanMapper::toSessionResponse)
                .toList();
    }

    @PatchMapping("/{planId}/sessions/{sessionId}/complete")
    @Operation(summary = "Marca uma sessão como concluída")
    @ApiResponse(responseCode = "200", description = "Sessão marcada como concluída")
    @ApiResponse(responseCode = "404", description = "Sessão não encontrada ou não pertence ao plano/usuário")
    @ApiResponse(responseCode = "409", description = "Sessão já está concluída")
    public TrainingSessionResponse completeSession(
            @PathVariable Long userId,
            @PathVariable Long planId,
            @PathVariable Long sessionId
    ) {
        TrainingSession session = trainingPlanService.completeSession(userId, planId, sessionId);
        return TrainingPlanMapper.toSessionResponse(session);
    }
}
