package com.wellness.ritmo.application.usecase.availability;

import com.wellness.ritmo.api.dto.AvailabilityRequestDto;
import com.wellness.ritmo.api.dto.AvailabilityResponseDto;
import com.wellness.ritmo.api.dto.mapper.AvailabilityMapper;
import com.wellness.ritmo.application.service.AuthenticatedUserService;
import com.wellness.ritmo.domain.model.UserAvailability;
import com.wellness.ritmo.domain.service.AvailabilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Use Case: Criar disponibilidade individual
 * 
 * Responsabilidade: Orquestrar a criação de um slot de disponibilidade.
 * 
 * PADRÃO DE ARQUITETURA LIMPA:
 * =============================
 * 1. AUTENTICA: Obtém o ID do usuário autenticado
 * 2. AUTORIZA: Valida que o usuário autenticado é o proprietário
 * 3. EXTRAI: Campos do DTO como primitivos
 * 4. DELEGA: Passa primitivos para AvailabilityService
 * 5. MAPEIA: Converte entidade para DTO de resposta
 * 6. RETORNA: AvailabilityResponseDto
 * 
 * @author Arquitetura Ritmo
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CreateAvailabilityUseCase {

    private final AvailabilityService availabilityService;
    private final AuthenticatedUserService authenticatedUserService;

    public AvailabilityResponseDto execute(Long userId, AvailabilityRequestDto requestDto) {
        log.info("[CreateAvailabilityUseCase] Criando disponibilidade para usuário: {}", userId);

        Long authenticatedUserId = authenticatedUserService.getCurrentUserId();
        authenticatedUserService.validateOwnership(authenticatedUserId, userId);

        UserAvailability availability = availabilityService.create(
            userId,
            requestDto.getDayOfWeek(),
            requestDto.getStartTime(),
            requestDto.getEndTime(),
            requestDto.getMaxSessionMinutes(),
            requestDto.getPreferredIntensity(),
            requestDto.getValidFrom(),
            requestDto.getValidUntil()
        );

        log.info("[CreateAvailabilityUseCase] Disponibilidade criada com sucesso. AvailabilityId: {}", availability.getId());

        return AvailabilityMapper.toDto(availability);
    }
}
