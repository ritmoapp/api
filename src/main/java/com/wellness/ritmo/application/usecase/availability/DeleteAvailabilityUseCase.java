package com.wellness.ritmo.application.usecase.availability;

import com.wellness.ritmo.application.service.AuthenticatedUserService;
import com.wellness.ritmo.domain.service.AvailabilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Use Case: Deletar disponibilidade
 * 
 * Responsabilidade: Orquestrar a remoção de um slot de disponibilidade.
 * 
 * SEGURANÇA:
 * ==========
 * ✓ Valida que o usuário autenticado é o proprietário
 * ✓ Impede acesso cruzado (XAC): um usuário não pode deletar availabilities de outro
 * 
 * @author Arquitetura Ritmo
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DeleteAvailabilityUseCase {

    private final AvailabilityService availabilityService;
    private final AuthenticatedUserService authenticatedUserService;

    public void execute(Long userId, Long availabilityId) {
        log.info("[DeleteAvailabilityUseCase] Deletando disponibilidade {} para usuário: {}", availabilityId, userId);

        Long authenticatedUserId = authenticatedUserService.getCurrentUserId();
        authenticatedUserService.validateOwnership(authenticatedUserId, userId);

        availabilityService.delete(userId, availabilityId);

        log.info("[DeleteAvailabilityUseCase] Disponibilidade {} deletada com sucesso para usuário: {}", 
                 availabilityId, userId);
    }
}
