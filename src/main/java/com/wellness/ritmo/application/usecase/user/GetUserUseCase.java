package com.wellness.ritmo.application.usecase.user;

import com.wellness.ritmo.api.dto.UserResponseDto;
import com.wellness.ritmo.api.dto.mapper.UserMapper;
import com.wellness.ritmo.domain.model.User;
import com.wellness.ritmo.domain.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Use Case: Recuperar usuário por ID
 * 
 * Responsabilidade: Orquestrar a recuperação de um usuário existente pelo seu ID.
 * 
 * POR QUE ENVOLVER OPERAÇÕES DE LEITURA EM USE CASES?
 * ====================================================
 * 
 * Embora operações de leitura sejam mais simples, envolvê-las em use cases
 * fornece:
 * 
 * 1. CONSISTÊNCIA: Todos os acessos a User passam pela mesma camada
 * 2. EXTENSIBILIDADE: Fácil adicionar caching, logging auditado, métricas
 * 3. MANUTENIBILIDADE: Mudanças futuro no acesso a dados precisam de um lugar
 * 4. PREPARAÇÃO: Foundation para features como:
 *    - Cache distribuído (Redis)
 *    - Event publishing (PublishUserRetrieved)
 *    - Audit logging (who accessed this user, when)
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
 *   [Domain Layer: UserService]
 *      - Find user by id
 *            ↓
 *   [Data Layer: Repository]
 *      - Fetch from database
 *            ↓
 *     HTTP Response (UserResponseDto)
 * 
 * @author Arquitetura Ritmo
 * @see UserService
 * @see UserResponseDto
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class GetUserUseCase {

    private final UserService userService;

    /**
     * Executa o caso de uso de recuperação de usuário.
     * 
     * @param userId ID do usuário a ser recuperado
     * @return UserResponseDto com os dados do usuário
     * 
     * @throws jakarta.persistence.EntityNotFoundException se o usuário não existir
     */
    public UserResponseDto execute(Long userId) {
        log.info("[GetUserUseCase] Recuperando usuário: {}", userId);

        // Delegar para serviço de domínio
        User user = userService.searchById(userId);

        log.debug("[GetUserUseCase] Usuário recuperado com sucesso. UserId: {}", user.getId());

        // Mapear para DTO de resposta
        return UserMapper.toDto(user);
    }
}
