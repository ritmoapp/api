package com.wellness.ritmo.api.controller;

import com.wellness.ritmo.api.dto.GoalRequestDto;
import com.wellness.ritmo.api.dto.GoalResponseDto;
import com.wellness.ritmo.application.service.AuthenticatedUserService;
import com.wellness.ritmo.application.usecase.goal.CancelGoalUseCase;
import com.wellness.ritmo.application.usecase.goal.CreateGoalUseCase;
import com.wellness.ritmo.application.usecase.goal.GetGoalUseCase;
import com.wellness.ritmo.application.usecase.goal.ListGoalsUseCase;
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
@RequestMapping("/users/{userId}/goals")
@RequiredArgsConstructor
@Tag(name = "Goals", description = "Gerenciamento de objetivos do usuário")
public class GoalController {

    private final CreateGoalUseCase createGoalUseCase;
    private final GetGoalUseCase getGoalUseCase;
    private final ListGoalsUseCase listGoalsUseCase;
    private final CancelGoalUseCase cancelGoalUseCase;
    private final AuthenticatedUserService authenticatedUserService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Criar objetivo do usuário",
            description = "Cria um novo objetivo (distância, pace, tempo, frequência ou prova). O userId na URL deve corresponder ao usuário autenticado."
    )
    @ApiResponse(responseCode = "201", description = "Objetivo criado com sucesso")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Acesso negado: você não pode criar objetivo para outro usuário")
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    @ApiResponse(responseCode = "400", description = "Campos obrigatórios ausentes para o tipo de objetivo")
    @ApiResponse(responseCode = "422", description = "Dados de entrada inválidos")
    public GoalResponseDto create(
            @PathVariable Long userId,
            @Valid @RequestBody GoalRequestDto requestDto) {
        
        Long authenticatedUserId = authenticatedUserService.getCurrentUserId();
        authenticatedUserService.validateOwnership(authenticatedUserId, userId);
        
        log.info("[GoalController] Criando objetivo para usuário: {}", userId);
        return createGoalUseCase.execute(userId, requestDto);
    }

    @GetMapping("/{goalId}")
    @Operation(
            summary = "Recuperar objetivo por ID",
            description = "Retorna um objetivo específico do usuário. O userId na URL deve corresponder ao usuário autenticado."
    )
    @ApiResponse(responseCode = "200", description = "Objetivo recuperado com sucesso")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Acesso negado: você não pode acessar objetivo de outro usuário")
    @ApiResponse(responseCode = "404", description = "Objetivo não encontrado")
    public GoalResponseDto getById(
            @PathVariable Long userId,
            @PathVariable Long goalId) {
        
        Long authenticatedUserId = authenticatedUserService.getCurrentUserId();
        authenticatedUserService.validateOwnership(authenticatedUserId, userId);
        
        log.debug("[GoalController] Recuperando objetivo {} para usuário: {}", goalId, userId);
        return getGoalUseCase.execute(userId, goalId);
    }

    @GetMapping
    @Operation(
            summary = "Listar objetivos do usuário",
            description = "Retorna todos os objetivos do usuário. O userId na URL deve corresponder ao usuário autenticado."
    )
    @ApiResponse(responseCode = "200", description = "Lista de objetivos recuperada com sucesso")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Acesso negado: você não pode acessar objetivos de outro usuário")
    public List<GoalResponseDto> getAll(@PathVariable Long userId) {
        
        Long authenticatedUserId = authenticatedUserService.getCurrentUserId();
        authenticatedUserService.validateOwnership(authenticatedUserId, userId);
        
        log.debug("[GoalController] Listando objetivos para usuário: {}", userId);
        return listGoalsUseCase.execute(userId);
    }

    @PatchMapping("/{goalId}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Cancelar objetivo",
            description = "Cancela um objetivo aberto do usuário. O userId na URL deve corresponder ao usuário autenticado."
    )
    @ApiResponse(responseCode = "204", description = "Objetivo cancelado com sucesso")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Acesso negado: você não pode cancelar objetivo de outro usuário")
    @ApiResponse(responseCode = "404", description = "Objetivo não encontrado")
    @ApiResponse(responseCode = "409", description = "Objetivo já completado, não pode ser cancelado")
    public void cancel(
            @PathVariable Long userId,
            @PathVariable Long goalId) {
        
        Long authenticatedUserId = authenticatedUserService.getCurrentUserId();
        authenticatedUserService.validateOwnership(authenticatedUserId, userId);
        
        log.info("[GoalController] Cancelando objetivo {} para usuário: {}", goalId, userId);
        cancelGoalUseCase.execute(userId, goalId);
    }
}
