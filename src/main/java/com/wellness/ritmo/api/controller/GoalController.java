package com.wellness.ritmo.api.controller;

import com.wellness.ritmo.api.dto.GoalRequestDto;
import com.wellness.ritmo.api.dto.GoalResponseDto;
import com.wellness.ritmo.api.dto.mapper.GoalMapper;
import com.wellness.ritmo.domain.model.Goal;
import com.wellness.ritmo.domain.service.GoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/goals")
@RequiredArgsConstructor
@Tag(name = "Goals", description = "Gerenciamento de objetivos do usuário")
public class GoalController {

    private final GoalService goalService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Criar objetivo do usuário",
            description = "Cria um novo objetivo (distância, pace, tempo, frequência ou prova)"
    )
    @ApiResponse(responseCode = "201", description = "Objetivo criado com sucesso")
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    @ApiResponse(responseCode = "400", description = "Campos obrigatórios ausentes para o tipo de objetivo")
    @ApiResponse(responseCode = "422", description = "Dados de entrada inválidos")
    public GoalResponseDto create(
            @PathVariable Long userId,
            @Valid @RequestBody GoalRequestDto requestDto) {
        Goal goal = goalService.create(userId, requestDto);
        return GoalMapper.toDto(goal);
    }

    @GetMapping("/{goalId}")
    @Operation(
            summary = "Recuperar objetivo por ID",
            description = "Retorna um objetivo específico do usuário"
    )
    @ApiResponse(responseCode = "200", description = "Objetivo recuperado com sucesso")
    @ApiResponse(responseCode = "404", description = "Objetivo não encontrado")
    public GoalResponseDto getById(
            @PathVariable Long userId,
            @PathVariable Long goalId) {
        Goal goal = goalService.getById(userId, goalId);
        return GoalMapper.toDto(goal);
    }

    @GetMapping
    @Operation(
            summary = "Listar objetivos do usuário",
            description = "Retorna todos os objetivos do usuário"
    )
    @ApiResponse(responseCode = "200", description = "Lista de objetivos recuperada com sucesso")
    public List<GoalResponseDto> getAll(@PathVariable Long userId) {
        return goalService.getAllByUser(userId).stream()
                .map(GoalMapper::toDto)
                .toList();
    }

    @PatchMapping("/{goalId}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Cancelar objetivo",
            description = "Cancela um objetivo aberto do usuário"
    )
    @ApiResponse(responseCode = "204", description = "Objetivo cancelado com sucesso")
    @ApiResponse(responseCode = "404", description = "Objetivo não encontrado")
    @ApiResponse(responseCode = "409", description = "Objetivo já completado, não pode ser cancelado")
    public void cancel(
            @PathVariable Long userId,
            @PathVariable Long goalId) {
        goalService.cancel(userId, goalId);
    }
}
