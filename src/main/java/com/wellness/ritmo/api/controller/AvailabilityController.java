package com.wellness.ritmo.api.controller;

import com.wellness.ritmo.api.dto.AvailabilityRequestDto;
import com.wellness.ritmo.api.dto.AvailabilityResponseDto;
import com.wellness.ritmo.application.service.AuthenticatedUserService;
import com.wellness.ritmo.application.usecase.availability.CreateAvailabilityBatchUseCase;
import com.wellness.ritmo.application.usecase.availability.CreateAvailabilityUseCase;
import com.wellness.ritmo.application.usecase.availability.DeleteAvailabilityUseCase;
import com.wellness.ritmo.application.usecase.availability.ListAvailabilitiesUseCase;
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
@RequestMapping("/users/{userId}/availabilities")
@RequiredArgsConstructor
@Tag(name = "Availability", description = "Gerenciamento de disponibilidade do usuário")
public class AvailabilityController {

    private final CreateAvailabilityUseCase createAvailabilityUseCase;
    private final CreateAvailabilityBatchUseCase createAvailabilityBatchUseCase;
    private final ListAvailabilitiesUseCase listAvailabilitiesUseCase;
    private final DeleteAvailabilityUseCase deleteAvailabilityUseCase;
    private final AuthenticatedUserService authenticatedUserService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Criar disponibilidade",
            description = "Adiciona um slot de disponibilidade para o usuário. O userId na URL deve corresponder ao usuário autenticado."
    )
    @ApiResponse(responseCode = "201", description = "Disponibilidade criada com sucesso")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Acesso negado: você não pode criar disponibilidade para outro usuário")
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    @ApiResponse(responseCode = "400", description = "Horário inválido")
    @ApiResponse(responseCode = "422", description = "Dados de entrada inválidos")
    public AvailabilityResponseDto create(
            @PathVariable Long userId,
            @Valid @RequestBody AvailabilityRequestDto requestDto) {
        
        Long authenticatedUserId = authenticatedUserService.getCurrentUserId();
        authenticatedUserService.validateOwnership(authenticatedUserId, userId);
        
        log.info("[AvailabilityController] Criando disponibilidade para usuário: {}", userId);
        return createAvailabilityUseCase.execute(userId, requestDto);
    }

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Criar múltiplas disponibilidades",
            description = "Adiciona vários slots de disponibilidade de uma vez (onboarding). O userId na URL deve corresponder ao usuário autenticado."
    )
    @ApiResponse(responseCode = "201", description = "Disponibilidades criadas com sucesso")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Acesso negado: você não pode criar disponibilidades para outro usuário")
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    public List<AvailabilityResponseDto> createBatch(
            @PathVariable Long userId,
            @Valid @RequestBody List<AvailabilityRequestDto> requestDtos) {
        
        Long authenticatedUserId = authenticatedUserService.getCurrentUserId();
        authenticatedUserService.validateOwnership(authenticatedUserId, userId);
        
        log.info("[AvailabilityController] Criando {} disponibilidades em lote para usuário: {}", requestDtos.size(), userId);
        return createAvailabilityBatchUseCase.execute(userId, requestDtos);
    }

    @GetMapping
    @Operation(
            summary = "Listar disponibilidades válidas",
            description = "Retorna as disponibilidades ativas do usuário. O userId na URL deve corresponder ao usuário autenticado."
    )
    @ApiResponse(responseCode = "200", description = "Lista recuperada com sucesso")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Acesso negado: você não pode acessar disponibilidades de outro usuário")
    public List<AvailabilityResponseDto> getValid(@PathVariable Long userId) {
        
        Long authenticatedUserId = authenticatedUserService.getCurrentUserId();
        authenticatedUserService.validateOwnership(authenticatedUserId, userId);
        
        log.debug("[AvailabilityController] Listando disponibilidades para usuário: {}", userId);
        return listAvailabilitiesUseCase.execute(userId);
    }

    @DeleteMapping("/{availabilityId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Remover disponibilidade",
            description = "Remove um slot de disponibilidade do usuário. O userId na URL deve corresponder ao usuário autenticado."
    )
    @ApiResponse(responseCode = "204", description = "Disponibilidade removida com sucesso")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Acesso negado: você não pode deletar disponibilidades de outro usuário")
    @ApiResponse(responseCode = "404", description = "Disponibilidade não encontrada")
    public void delete(
            @PathVariable Long userId,
            @PathVariable Long availabilityId) {
        
        Long authenticatedUserId = authenticatedUserService.getCurrentUserId();
        authenticatedUserService.validateOwnership(authenticatedUserId, userId);
        
        log.info("[AvailabilityController] Deletando disponibilidade {} para usuário: {}", availabilityId, userId);
        deleteAvailabilityUseCase.execute(userId, availabilityId);
    }
}
