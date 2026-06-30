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

import java.util.List;

/**
 * Use Case: Criar múltiplas disponibilidades em lote
 * 
 * Responsabilidade: Orquestrar a criação em lote de slots de disponibilidade.
 * Utilizado durante onboarding para carregar calendário de disponibilidade.
 * 
 * PADRÃO DE ARQUITETURA LIMPA:
 * =============================
 * 1. AUTENTICA: Obtém o ID do usuário autenticado
 * 2. AUTORIZA: Valida que o usuário autenticado é o proprietário
 * 3. EXTRAI: Converte cada DTO em primitivos
 * 4. DELEGA: Passa lista de AvailabilityData para AvailabilityService
 * 5. MAPEIA: Converte entidades para DTOs de resposta
 * 6. RETORNA: Lista de AvailabilityResponseDto
 * 
 * @author Arquitetura Ritmo
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CreateAvailabilityBatchUseCase {

    private final AvailabilityService availabilityService;
    private final AuthenticatedUserService authenticatedUserService;

    public List<AvailabilityResponseDto> execute(Long userId, List<AvailabilityRequestDto> requestDtos) {
        log.info("[CreateAvailabilityBatchUseCase] Criando {} disponibilidades para usuário: {}", 
                 requestDtos.size(), userId);

        Long authenticatedUserId = authenticatedUserService.getCurrentUserId();
        authenticatedUserService.validateOwnership(authenticatedUserId, userId);

        List<AvailabilityService.AvailabilityData> availabilityDataList = requestDtos.stream()
            .map(dto -> new AvailabilityService.AvailabilityData(
                dto.getDayOfWeek(),
                dto.getStartTime(),
                dto.getEndTime(),
                dto.getMaxSessionMinutes(),
                dto.getPreferredIntensity(),
                dto.getValidFrom(),
                dto.getValidUntil()
            ))
            .toList();

        List<UserAvailability> availabilities = availabilityService.createBatch(userId, availabilityDataList);

        log.info("[CreateAvailabilityBatchUseCase] {} disponibilidades criadas com sucesso", availabilities.size());

        return availabilities.stream()
            .map(AvailabilityMapper::toDto)
            .toList();
    }
}
