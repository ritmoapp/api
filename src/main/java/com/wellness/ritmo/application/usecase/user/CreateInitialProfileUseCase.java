package com.wellness.ritmo.application.usecase.user;

import com.wellness.ritmo.api.dto.OnboardingDto;
import com.wellness.ritmo.api.dto.UserProfileResponseDto;
import com.wellness.ritmo.api.dto.mapper.UserProfileMapper;
import com.wellness.ritmo.domain.model.UserProfile;
import com.wellness.ritmo.domain.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Use Case: Criar Perfil Inicial de Usuário (Onboarding)
 * 
 * Responsabilidade: Orquestrar a criação do perfil inicial durante o processo
 * de onboarding de um novo usuário.
 * 
 * PADRÃO DE ARQUITETURA LIMPA:
 * =============================
 * Este use case atua como intermediário entre a camada de API (DTOs) e a camada
 * de domínio (serviços puros). Ele:
 * 
 * 1. RECEBE: OnboardingDto (objeto específico de HTTP, com validações de API)
 * 2. EXTRAI: Campos individuais do DTO como primitivos/enums
 * 3. DELEGA: Passa primitivos para UserProfileService (não o DTO inteiro)
 * 4. MAPEIA: Converte entidade de domínio para DTO de resposta
 * 5. RETORNA: UserProfileResponseDto (seguro de dados)
 * 
 * BENEFÍCIOS:
 * -----------
 * ✓ Camada de domínio permanece PURA: não conhece conceitos de HTTP (DTOs)
 * ✓ Serviço pode ser reutilizado em contextos não-REST (CLI, agendamentos, eventos)
 * ✓ Facilita testes: service pode ser testado passando primitivos, sem Spring
 * ✓ Preparado para extensão: pode adicionar logging, eventos, validação orquestrada
 * 
 * FLUXO DE DADOS:
 * ---------------
 * 
 *     HTTP Request (OnboardingDto)
 *            ↓
 *   [API Layer: Controller]
 *            ↓
 *   [Application Layer: This Use Case] ← Você está aqui
 *      - Extract: dto.getGender() → Gender gender
 *      - Extract: dto.getPaceAvgSeg() → Integer paceAvgSeg
 *      - ...mais campos
 *            ↓
 *   [Domain Layer: UserProfileService]
 *      - Pure business logic (sem DTO)
 *      - Validações de negócio (pace vs conditioning level)
 *      - Criação e persistência
 *            ↓
 *   [Data Layer: Repository]
 *      - Salva UserProfile no banco
 *            ↓
 *     HTTP Response (UserProfileResponseDto)
 * 
 * EXEMPLO DE USO:
 * ---------------
 * 
 * No Controller:
 * <pre>
 * @PostMapping
 * public UserProfileResponseDto createProfile(
 *     @PathVariable Long userId,
 *     @Valid @RequestBody OnboardingDto dto
 * ) {
 *     return createInitialProfileUseCase.execute(userId, dto);
 * }
 * </pre>
 * 
 * Aqui neste Use Case:
 * <pre>
 * UserProfile profile = userProfileService.createInitialProfile(
 *     userId,
 *     dto.getGender(),        // ← Primitivo, não DTO
 *     dto.getBirthDate(),     // ← Primitivo, não DTO
 *     dto.getHeightCm(),      // ← Primitivo, não DTO
 *     dto.getWeightKg(),      // ← Primitivo, não DTO
 *     ...
 * );
 * </pre>
 * 
 * @author Arquitetura Ritmo
 * @see UserProfileService
 * @see OnboardingDto
 * @see UserProfileResponseDto
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CreateInitialProfileUseCase {

    private final UserProfileService userProfileService;

    /**
     * Executa o caso de uso de criação de perfil inicial.
     * 
     * @param userId ID do usuário para o qual criar o perfil
     * @param onboardingDto Dados de onboarding vindos da requisição HTTP
     * @return UserProfileResponseDto com os dados do perfil criado
     * 
     * @throws jakarta.persistence.EntityNotFoundException se o usuário não existir
     * @throws IllegalStateException se o usuário já possui um perfil
     * @throws IllegalArgumentException se o pace for incompatível com conditioning level
     */
    public UserProfileResponseDto execute(Long userId, OnboardingDto onboardingDto) {
        log.info("[CreateInitialProfileUseCase] Iniciando criação de perfil para usuário: {}", userId);

        // PASSO 1: Extrair campos do DTO como primitivos
        // Isso garante que a camada de domínio não dependa de DTOs
        var profile = userProfileService.createInitialProfile(
            userId,
            onboardingDto.getGender(),
            onboardingDto.getBirthDate(),
            onboardingDto.getHeightCm(),
            onboardingDto.getWeightKg(),
            onboardingDto.getConditioningLevel(),
            onboardingDto.getPaceAvgSeg(),
            onboardingDto.getWeeklyMileageKm()
        );

        log.info("[CreateInitialProfileUseCase] Perfil criado com sucesso. ProfileId: {}", profile.getId());

        // PASSO 2: Mapear entidade de domínio para DTO de resposta
        return UserProfileMapper.toDto(profile);
    }
}
