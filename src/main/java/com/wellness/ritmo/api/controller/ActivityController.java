package com.wellness.ritmo.api.controller;

import com.wellness.ritmo.api.dto.ActivityRequestDto;
import com.wellness.ritmo.api.dto.ActivityResponseDto;
import com.wellness.ritmo.application.service.AuthenticatedUserService;
import com.wellness.ritmo.application.usecase.activity.RegisterActivityUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Controller para gerenciar o registro de atividades (exercícios).
 * 
 * SEGURANÇA:
 * ==========
 * ✓ Todas as operações verificam se o usuário autenticado é o proprietário
 * ✓ Usa AuthenticatedUserService.validateOwnership() para prevenir XAC
 * 
 * @author Arquitetura Ritmo
 */
@Slf4j
@RestController
@RequestMapping("/users/{userId}/activities")
@RequiredArgsConstructor
@Tag(name = "Activities", description = "Registro e consulta de treinos realizados")
public class ActivityController {

    private final RegisterActivityUseCase registerActivityUseCase;
    private final AuthenticatedUserService authenticatedUserService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Registrar treino concluído",
            description = "Salva a activity, avalia meta associada e dispara análise IA em background. O userId na URL deve corresponder ao usuário autenticado."
    )
    @ApiResponse(responseCode = "201", description = "Atividade registrada com sucesso")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Acesso negado: você não pode registrar atividades para outro usuário")
    @ApiResponse(responseCode = "404", description = "Usuário ou meta não encontrado")
    @ApiResponse(responseCode = "422", description = "Dados de entrada inválidos")
    public ActivityResponseDto register(
            @PathVariable Long userId,
            @Valid @RequestBody ActivityRequestDto dto
    ) {
        Long authenticatedUserId = authenticatedUserService.getCurrentUserId();
        authenticatedUserService.validateOwnership(authenticatedUserId, userId);
        
        log.info("[ActivityController] Registrando atividade para usuário: {}", userId);
        return registerActivityUseCase.execute(userId, dto);
    }
}

