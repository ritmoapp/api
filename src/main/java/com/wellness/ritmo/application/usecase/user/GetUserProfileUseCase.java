package com.wellness.ritmo.application.usecase.user;

import com.wellness.ritmo.api.dto.UserProfileResponseDto;
import com.wellness.ritmo.api.dto.mapper.UserProfileMapper;
import com.wellness.ritmo.domain.model.UserProfile;
import com.wellness.ritmo.domain.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Use Case: Recuperar Perfil de Usuário
 * 
 * Responsabilidade: Orquestrar a recuperação do perfil de um usuário existente.
 * 
 * POR QUE ENVOLVER OPERAÇÕES DE LEITURA EM USE CASES?
 * ====================================================
 * 
 * Embora operações de leitura sejam mais simples, envolvê-las em use cases
 * fornece:
 * 
 * 1. CONSISTÊNCIA: Todos os acessos a UserProfile passam pela mesma camada
 * 2. EXTENSIBILIDADE: Fácil adicionar caching, logging auditado, métricas
 * 3. MANUTENIBILIDADE: Mudanças futuro no acesso a dados precisam de um lugar
 * 4. PREPARAÇÃO: Foundation para features como:
 *    - Cache distribuído (Redis)
 *    - Event publishing (PublishProfileRetrieved)
 *    - Audit logging (who accessed this profile, when)
 *    - Permission checks (user can access own profile?)
 * 
 * FLUXO SIMPLES (Comparado com Create/Update):
 * =============================================
 * 
 *     HTTP GET Request
 *            ↓
 *   [API Layer: Controller]
 *            ↓
 *   [Application Layer: This Use Case]
 *      - Simple delegation
 *      - Could add cache check here
 *            ↓
 *   [Domain Layer: UserProfileService]
 *      - Find profile by userId
 *            ↓
 *   [Data Layer: Repository]
 *      - Fetch from database
 *            ↓
 *     HTTP Response (UserProfileResponseDto)
 * 
 * EXEMPLO DE EXTENSÃO FUTURA:
 * ============================
 * 
 * // Com caching:
 * public UserProfileResponseDto execute(Long userId) {
 *     var cachedProfile = cacheService.get("profile:" + userId);
 *     if (cachedProfile != null) {
 *         log.debug("Cache hit for profile: {}", userId);
 *         return cachedProfile;
 *     }
 *     
 *     var profile = userProfileService.getProfileByUserId(userId);
 *     cacheService.set("profile:" + userId, profile, Duration.ofHours(1));
 *     return UserProfileMapper.toDto(profile);
 * }
 * 
 * // Com event publishing:
 * public UserProfileResponseDto execute(Long userId) {
 *     var profile = userProfileService.getProfileByUserId(userId);
 *     eventPublisher.publishEvent(new ProfileAccessedEvent(userId, Instant.now()));
 *     return UserProfileMapper.toDto(profile);
 * }
 * 
 * @author Arquitetura Ritmo
 * @see UserProfileService
 * @see UserProfileResponseDto
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class GetUserProfileUseCase {

    private final UserProfileService userProfileService;

    /**
     * Executa o caso de uso de recuperação de perfil.
     * 
     * @param userId ID do usuário cujo perfil será recuperado
     * @return UserProfileResponseDto com os dados do perfil
     * 
     * @throws jakarta.persistence.EntityNotFoundException se o perfil não existir
     */
    public UserProfileResponseDto execute(Long userId) {
        log.info("[GetUserProfileUseCase] Recuperando perfil para usuário: {}", userId);

        // Delegar para serviço de domínio
        UserProfile profile = userProfileService.getProfileByUserId(userId);

        log.debug("[GetUserProfileUseCase] Perfil recuperado com sucesso. ProfileId: {}", profile.getId());

        // Mapear para DTO de resposta
        return UserProfileMapper.toDto(profile);
    }
}
