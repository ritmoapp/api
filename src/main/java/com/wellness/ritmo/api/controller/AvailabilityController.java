package com.wellness.ritmo.api.controller;

import com.wellness.ritmo.api.dto.AvailabilityRequestDto;
import com.wellness.ritmo.api.dto.AvailabilityResponseDto;
import com.wellness.ritmo.api.dto.mapper.AvailabilityMapper;
import com.wellness.ritmo.domain.model.UserAvailability;
import com.wellness.ritmo.domain.service.AvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/availabilities")
@RequiredArgsConstructor
@Tag(name = "Availability", description = "Gerenciamento de disponibilidade do usuário")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Criar disponibilidade",
            description = "Adiciona um slot de disponibilidade para o usuário"
    )
    @ApiResponse(responseCode = "201", description = "Disponibilidade criada com sucesso")
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    @ApiResponse(responseCode = "400", description = "Horário inválido")
    @ApiResponse(responseCode = "422", description = "Dados de entrada inválidos")
    public AvailabilityResponseDto create(
            @PathVariable Long userId,
            @Valid @RequestBody AvailabilityRequestDto requestDto) {
        UserAvailability availability = availabilityService.create(userId, requestDto);
        return AvailabilityMapper.toDto(availability);
    }

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Criar múltiplas disponibilidades",
            description = "Adiciona vários slots de disponibilidade de uma vez (onboarding)"
    )
    @ApiResponse(responseCode = "201", description = "Disponibilidades criadas com sucesso")
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    public List<AvailabilityResponseDto> createBatch(
            @PathVariable Long userId,
            @Valid @RequestBody List<AvailabilityRequestDto> requestDtos) {
        return availabilityService.createBatch(userId, requestDtos).stream()
                .map(AvailabilityMapper::toDto)
                .toList();
    }

    @GetMapping
    @Operation(
            summary = "Listar disponibilidades válidas",
            description = "Retorna as disponibilidades ativas do usuário"
    )
    @ApiResponse(responseCode = "200", description = "Lista recuperada com sucesso")
    public List<AvailabilityResponseDto> getValid(@PathVariable Long userId) {
        return availabilityService.getValidByUser(userId).stream()
                .map(AvailabilityMapper::toDto)
                .toList();
    }

    @DeleteMapping("/{availabilityId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Remover disponibilidade",
            description = "Remove um slot de disponibilidade do usuário"
    )
    @ApiResponse(responseCode = "204", description = "Disponibilidade removida com sucesso")
    @ApiResponse(responseCode = "404", description = "Disponibilidade não encontrada")
    public void delete(
            @PathVariable Long userId,
            @PathVariable Long availabilityId) {
        availabilityService.delete(userId, availabilityId);
    }
}
