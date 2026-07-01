package com.wellness.ritmo.application.usecase.user;

import com.wellness.ritmo.api.dto.UserCreateDto;
import com.wellness.ritmo.api.dto.UserResponseDto;
import com.wellness.ritmo.api.dto.mapper.UserMapper;
import com.wellness.ritmo.domain.model.User;
import com.wellness.ritmo.domain.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Use Case: Criar novo usuário (registro)
 * 
 * Responsabilidade: Orquestrar a criação de um novo usuário durante o processo
 * de registro.
 * 
 * PADRÃO DE ARQUITETURA LIMPA:
 * =============================
 * Este use case atua como intermediário entre a camada de API (DTOs) e a camada
 * de domínio (serviços puros). Ele:
 * 
 * 1. RECEBE: UserCreateDto (objeto específico de HTTP, com validações de API)
 * 2. EXTRAI: Campos individuais do DTO como primitivos
 * 3. DELEGA: Passa primitivos para UserService (não o DTO inteiro)
 * 4. MAPEIA: Converte entidade de domínio para DTO de resposta
 * 5. RETORNA: UserResponseDto (seguro de dados)
 * 
 * BENEFÍCIOS:
 * -----------
 * ✓ Camada de domínio permanece PURA: não conhece conceitos de HTTP (DTOs)
 * ✓ Serviço pode ser reutilizado em contextos não-REST (CLI, agendamentos, eventos)
 * ✓ Facilita testes: service pode ser testado passando primitivos, sem Spring
 * ✓ Preparado para extensão: pode adicionar logging, validação orquestrada, eventos
 * 
 * FLUXO DE DADOS:
 * ---------------
 * 
 *     HTTP Request (UserCreateDto)
 *            ↓
 *   [API Layer: Controller]
 *            ↓
 *   [Application Layer: This Use Case] ← Você está aqui
 *      - Extract: dto.getUserName() → String username
 *      - Extract: dto.getEmail() → String email
 *      - Extract: dto.getPassword() → String password
 *            ↓
 *   [Domain Layer: UserService]
 *      - Validação de negócio (email único, username único)
 *      - Hash de senha
 *      - Criação e persistência
 *            ↓
 *   [Data Layer: Repository]
 *      - Salva User no banco
 *            ↓
 *     HTTP Response (UserResponseDto)
 * 
 * EXEMPLO DE USO:
 * ---------------
 * 
 * No Controller:
 * <pre>
 * @PostMapping
 * public UserResponseDto create(
 *     @Valid @RequestBody UserCreateDto createDto
 * ) {
 *     return createUserUseCase.execute(createDto);
 * }
 * </pre>
 * 
 * Aqui neste Use Case:
 * <pre>
 * User user = userService.save(
 *     dto.getUserName(),    // ← Primitivo, não DTO
 *     dto.getEmail(),       // ← Primitivo, não DTO
 *     dto.getPassword()     // ← Primitivo, não DTO
 * );
 * </pre>
 * 
 * @author Arquitetura Ritmo
 * @see UserService
 * @see UserCreateDto
 * @see UserResponseDto
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CreateUserUseCase {

    private final UserService userService;

    /**
     * Executa o caso de uso de criação de novo usuário.
     * 
     * @param createDto Dados de criação vindos da requisição HTTP
     * @return UserResponseDto com os dados do usuário criado
     * 
     * @throws com.wellness.ritmo.api.exception.UserAlreadyExistsException se o email ou username já existem
     */
    public UserResponseDto execute(UserCreateDto createDto) {
        log.info("[CreateUserUseCase] Iniciando criação de novo usuário: {}", createDto.getUserName());

        // PASSO 1: Extrair campos do DTO como primitivos
        // Isso garante que a camada de domínio não dependa de DTOs
        User user = userService.save(
            createDto.getUserName(),
            createDto.getEmail(),
            createDto.getPassword()
        );

        log.info("[CreateUserUseCase] Usuário criado com sucesso. UserId: {}", user.getId());

        // PASSO 2: Mapear entidade de domínio para DTO de resposta
        return UserMapper.toDto(user);
    }
}
