package com.wellness.ritmo.application.service;

import com.wellness.ritmo.domain.model.User;
import com.wellness.ritmo.domain.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Serviço para gerenciar operações relacionadas ao usuário autenticado.
 * 
 * RESPONSABILIDADES:
 * ===================
 * 1. Extrair informações do usuário autenticado do contexto Spring Security
 * 2. Recuperar a entidade User completa do banco de dados
 * 3. Validar autorização (verificar propriedade de recurso)
 * 
 * PADRÃO DE USO:
 * ==============
 * No Controller:
 * <pre>
 * public ResponseDto endpoint() {
 *     Long currentUserId = authenticatedUserService.getCurrentUserId();
 *     User currentUser = authenticatedUserService.getCurrentUser();
 *     
 *     // Validar propriedade
 *     authenticatedUserService.validateOwnership(currentUserId, targetUserId);
 * }
 * </pre>
 * 
 * SEGURANÇA:
 * ==========
 * ✓ Garante que o usuário autenticado é o proprietário do recurso
 * ✓ Impede acesso a recursos de outros usuários (XAC - Cross-Account Access)
 * ✓ Centraliza lógica de autorização (DRY - Don't Repeat Yourself)
 * 
 * @author Arquitetura Ritmo
 * @see UpdateUserProfileUseCase
 * @see CreateInitialProfileUseCase
 * @see GetUserProfileUseCase
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AuthenticatedUserService {

    private final UserRepository userRepository;

    /**
     * Obtém o ID do usuário autenticado a partir do contexto Spring Security.
     * 
     * @return ID do usuário autenticado
     * @throws IllegalStateException se nenhum usuário está autenticado
     */
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("[AuthenticatedUserService] Tentativa de acesso sem autenticação");
            throw new IllegalStateException("Usuário não autenticado");
        }

        String username = authentication.getName();
        log.debug("[AuthenticatedUserService] Recuperando ID para usuário: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("[AuthenticatedUserService] Usuário autenticado não encontrado no DB: {}", username);
                    return new EntityNotFoundException("Usuário autenticado não encontrado: " + username);
                });

        return user.getId();
    }

    /**
     * Obtém a entidade User completa do usuário autenticado.
     * 
     * @return Entidade User do usuário autenticado
     * @throws IllegalStateException se nenhum usuário está autenticado
     * @throws EntityNotFoundException se o usuário não existe no banco
     */
    public User getCurrentUser() {
        Long userId = getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                    "Usuário autenticado não encontrado no banco: " + userId
                ));
    }

    /**
     * Valida se o usuário autenticado é o proprietário de um recurso.
     * 
     * EXEMPLO:
     * <pre>
     * Long currentUserId = authenticatedUserService.getCurrentUserId();
     * authenticatedUserService.validateOwnership(currentUserId, resourceOwnerId);
     * // Se currentUserId != resourceOwnerId → lança AccessDeniedException
     * </pre>
     * 
     * @param authenticatedUserId ID do usuário autenticado (obtido via getCurrentUserId())
     * @param resourceOwnerId ID do proprietário do recurso
     * @throws AccessDeniedException se os IDs não correspondem (não é o proprietário)
     */
    public void validateOwnership(Long authenticatedUserId, Long resourceOwnerId) {
        if (!authenticatedUserId.equals(resourceOwnerId)) {
            log.warn(
                "[AuthenticatedUserService] Acesso negado: Usuário {} tentou acessar recurso do usuário {}",
                authenticatedUserId,
                resourceOwnerId
            );
            throw new AccessDeniedException(
                "Você não tem permissão para acessar recursos de outro usuário"
            );
        }
        
        log.debug("[AuthenticatedUserService] Validação de propriedade OK: usuário {}", authenticatedUserId);
    }
}
