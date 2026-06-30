package com.wellness.ritmo.application.usecase.user;

import com.wellness.ritmo.api.dto.UserProfileResponseDto;
import com.wellness.ritmo.api.dto.UserProfileUpdateDto;
import com.wellness.ritmo.api.dto.mapper.UserProfileMapper;
import com.wellness.ritmo.domain.model.UserProfile;
import com.wellness.ritmo.domain.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Use Case: Atualizar Perfil de Usuário
 * 
 * Responsabilidade: Orquestrar a atualização parcial de um perfil de usuário
 * existente.
 * 
 * DIFERENÇA IMPORTANTE vs CreateInitialProfileUseCase:
 * =====================================================
 * 
 * Na CRIAÇÃO (CreateInitialProfileUseCase):
 * - Todos os campos são obrigatórios
 * - Aplicamos todas as validações estritas
 * - Nenhum campo pode ser omitido
 * 
 * Na ATUALIZAÇÃO (Este use case):
 * - Todos os campos são OPCIONAIS
 * - Campos omitidos (null) = "não atualizar"
 * - Apenas campos fornecidos são atualizados
 * - UserProfileUpdateDto EXCLUI campos imutáveis (gender, birthDate, heightCm)
 * 
 * EXEMPLO DE ATUALIZAÇÃO PARCIAL:
 * --------------------------------
 * 
 * Cliente envia:
 * <pre>
 * PUT /users/123/profile
 * {
 *   "weightKg": 65.0,
 *   "paceAvgSeg": 340
 * }
 * </pre>
 * 
 * Resultado:
 * - weightKg é atualizado para 65.0
 * - paceAvgSeg é atualizado para 340
 * - conditioningLevel permanece inalterado (null no DTO = não toca)
 * - weeklyMileageKm permanece inalterado (null no DTO = não toca)
 * 
 * ARQUITETURA LIMPA (Mesma que CreateInitialProfileUseCase):
 * ===========================================================
 * 
 * 1. RECEBE: UserProfileUpdateDto (apenas campos mutáveis)
 * 2. EXTRAI: Apenas valores não-null do DTO
 * 3. DELEGA: Passa primitivos para UserProfileService
 * 4. MAPEIA: Converte resposta para DTO
 * 5. RETORNA: UserProfileResponseDto
 * 
 * FLUXO DE DADOS:
 * ---------------
 * 
 *     HTTP Request (UserProfileUpdateDto)
 *     { "weightKg": 65.0, "paceAvgSeg": 340 }
 *            ↓
 *   [API Layer: Controller]
 *            ↓
 *   [Application Layer: This Use Case] ← Você está aqui
 *      - Check: dto.getWeightKg() != null → "update it"
 *      - Check: dto.getConditioningLevel() == null → "skip it"
 *      - Extract non-null values only
 *            ↓
 *   [Domain Layer: UserProfileService]
 *      - Update only specified fields
 *      - Validate pace vs conditioning level if both present
 *      - Persist changes
 *            ↓
 *   [Data Layer: Repository]
 *      - Save updated UserProfile
 *            ↓
 *     HTTP Response (UserProfileResponseDto)
 * 
 * VALIDAÇÃO E LÓGICA DE NEGÓCIO:
 * ===============================
 * 
 * O serviço de domínio valida:
 * - Se paceAvgSeg foi fornecido, valida contra conditioning level
 *   - Se conditioning level também foi fornecido, valida contra o novo nível
 *   - Se não foi fornecido, valida contra o nível EXISTENTE
 * 
 * Exemplo:
 * <pre>
 * Perfil atual:
 *   conditioningLevel = INTERMEDIATE (faixa pace: 300-480 seg)
 *   paceAvgSeg = 380
 * 
 * Update fornecido:
 *   paceAvgSeg = 280 (muito rápido para INTERMEDIATE!)
 *   conditioningLevel = null (não está mudando o nível)
 * 
 * Resultado:
 *   ✗ ERRO: Pace 280 incompatível com nível INTERMEDIATE
 *     Deve estar entre 300-480 seg/km
 * </pre>
 * 
 * @author Arquitetura Ritmo
 * @see UserProfileService
 * @see UserProfileUpdateDto
 * @see UserProfileResponseDto
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class UpdateUserProfileUseCase {

    private final UserProfileService userProfileService;

    /**
     * Executa o caso de uso de atualização de perfil.
     * 
     * @param userId ID do usuário cujo perfil será atualizado
     * @param updateDto Dados de atualização (campos opcionais)
     * @return UserProfileResponseDto com o perfil atualizado
     * 
     * @throws jakarta.persistence.EntityNotFoundException se o perfil não existir
     * @throws IllegalArgumentException se o pace for incompatível com conditioning level
     */
    public UserProfileResponseDto execute(Long userId, UserProfileUpdateDto updateDto) {
        log.info("[UpdateUserProfileUseCase] Iniciando atualização de perfil para usuário: {}", userId);

        // PASSO 1: Extrair campos NÃO-NULOS do DTO
        // Campos null significam "não atualizar este campo"
        var profile = userProfileService.updateProfile(
            userId,
            updateDto.getWeightKg(),
            updateDto.getConditioningLevel(),
            updateDto.getPaceAvgSeg(),
            updateDto.getWeeklyMileageKm()
        );

        log.info("[UpdateUserProfileUseCase] Perfil atualizado com sucesso. ProfileId: {}", profile.getId());

        // PASSO 2: Mapear entidade de domínio para DTO de resposta
        return UserProfileMapper.toDto(profile);
    }
}
