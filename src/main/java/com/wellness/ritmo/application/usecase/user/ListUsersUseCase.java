package com.wellness.ritmo.application.usecase.user;

import com.wellness.ritmo.api.dto.UserResponseDto;
import com.wellness.ritmo.api.dto.mapper.UserMapper;
import com.wellness.ritmo.domain.model.User;
import com.wellness.ritmo.domain.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Use Case: Listar usuários com paginação
 * 
 * Responsabilidade: Orquestrar a recuperação de uma lista paginada de usuários.
 * 
 * BENEFÍCIOS DE USE CASE PARA LISTAGEM:
 * =====================================
 * 
 * 1. SEPARAÇÃO DE RESPONSABILIDADES: Controller não precisa saber como mapear
 * 2. CONSISTÊNCIA: Todas as listas passam pela mesma transformação
 * 3. EXTENSIBILIDADE: Fácil adicionar:
 *    - Filtros (por status, data de criação, etc)
 *    - Ordenação padrão
 *    - Auditoria
 *    - Metricas (quantos usuarios foram listados)
 * 
 * FLUXO:
 * ======
 * 
 *     HTTP GET /users?page=0&size=10
 *            ↓
 *   [API Layer: Controller]
 *      - Extrai Pageable
 *            ↓
 *   [Application Layer: This Use Case]
 *      - Chama UserService.getAll(pageable)
 *      - Mapeia cada User para UserResponseDto
 *            ↓
 *   [Domain Layer: UserService]
 *      - Delega para repository
 *            ↓
 *   [Data Layer: Repository]
 *      - Executa query paginada
 *            ↓
 *     HTTP Response (Page<UserResponseDto>)
 * 
 * @author Arquitetura Ritmo
 * @see UserService
 * @see UserResponseDto
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ListUsersUseCase {

    private final UserService userService;

    /**
     * Executa o caso de uso de listagem paginada de usuários.
     * 
     * @param pageable Informações de paginação e ordenação
     * @return Page<UserResponseDto> com os dados dos usuários
     */
    public Page<UserResponseDto> execute(Pageable pageable) {
        log.info("[ListUsersUseCase] Listando usuários. Page: {}, Size: {}", 
            pageable.getPageNumber(), pageable.getPageSize());

        // Delegar para serviço de domínio
        Page<User> users = userService.getAll(pageable);

        log.debug("[ListUsersUseCase] {} usuários recuperados", users.getNumberOfElements());

        // Mapear cada entidade para DTO de resposta
        return users.map(UserMapper::toDto);
    }
}
