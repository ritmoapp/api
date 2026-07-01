package com.wellness.ritmo.application.usecase.availability;

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
 * Use Case: Listar disponibilidades válidas do usuário
 * 
 * Responsabilidade: Orquestrar a recuperação de slots de disponibilidade ativos.
 * 
 * SEGURANÇA:
 * ==========
 * ✓ Valida que o usuário autenticado é o proprietário
 * ✓ Impede acesso cruzado (XAC): um usuário não pode listar availabilities de outro
 * 
 * @author Arquitetura Ritmo
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ListAvailabilitiesUseCase {

    private final AvailabilityService availabilityService;
    private final AuthenticatedUserService authenticatedUserService;

    public List<AvailabilityResponseDto> execute(Long userId) {
        log.info("[ListAvailabilitiesUseCase] Listando disponibilidades para usuário: {}", userId);

        Long authenticatedUserId = authenticatedUserService.getCurrentUserId();
        authenticatedUserService.validateOwnership(authenticatedUserId, userId);

        List<UserAvailability> availabilities = availabilityService.getValidByUser(userId);

        log.debug("[ListAvailabilitiesUseCase] {} disponibilidade(s) recuperada(s) para usuário: {}", 
                  availabilities.size(), userId);

        return availabilities.stream()
            .map(AvailabilityMapper::toDto)
            .toList();
    }
}
