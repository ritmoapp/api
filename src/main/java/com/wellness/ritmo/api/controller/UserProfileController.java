package com.wellness.ritmo.api.controller;

import com.wellness.ritmo.api.dto.OnboardingDto;
import com.wellness.ritmo.api.dto.UserProfileResponseDto;
import com.wellness.ritmo.api.dto.UserProfileUpdateDto;
import com.wellness.ritmo.application.service.AuthenticatedUserService;
import com.wellness.ritmo.application.usecase.user.CreateInitialProfileUseCase;
import com.wellness.ritmo.application.usecase.user.GetUserProfileUseCase;
import com.wellness.ritmo.application.usecase.user.UpdateUserProfileUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Controller para gerenciar o perfil do usuário.
 * 
 * SEGURANÇA:
 * ==========
 * ✓ Todas as operações verificam se o usuário autenticado é o proprietário
 * ✓ Endpoints "POST /profile", "PUT /profile", "GET /profile" derivam userId da autenticação
 * ✓ Endpoints antigos com path "{userId}" ainda funcionam mas com validação de propriedade
 * 
 * ENDPOINTS:
 * ==========
 * 
 * POST   /users/{userId}/profile  → Criar perfil (requer propriedade)
 * POST   /profile                 → Criar perfil para usuário autenticado
 * 
 * PUT    /users/{userId}/profile  → Atualizar perfil (requer propriedade)
 * PUT    /profile                 → Atualizar perfil de usuário autenticado
 * 
 * GET    /users/{userId}/profile  → Obter perfil (requer propriedade)
 * GET    /profile                 → Obter perfil de usuário autenticado
 * 
 * @author Arquitetura Ritmo
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "Gerenciamento de perfil do usuário")
public class UserProfileController {

    private final CreateInitialProfileUseCase createInitialProfileUseCase;
    private final UpdateUserProfileUseCase updateUserProfileUseCase;
    private final GetUserProfileUseCase getUserProfileUseCase;
    private final AuthenticatedUserService authenticatedUserService;

    // ============================================================================
    // ENDPOINTS RECOMENDADOS (Seguro - derivam userId da autenticação)
    // ============================================================================

    @PostMapping("/profile")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Criar perfil inicial do usuário (onboarding) - RECOMENDADO",
            description = "Cria o perfil inicial para o usuário autenticado. O userId é derivado da autenticação."
    )
    @ApiResponse(responseCode = "201", description = "Perfil criado com sucesso")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "409", description = "Usuário já possui um perfil")
    @ApiResponse(responseCode = "422", description = "Dados de entrada inválidos ou pace incompatível com nível de condicionamento")
    public UserProfileResponseDto createProfileForAuthenticatedUser(
            @Valid @RequestBody OnboardingDto onboardingDto) {

        Long authenticatedUserId = authenticatedUserService.getCurrentUserId();
        log.info("[UserProfileController] Criando perfil para usuário autenticado: {}", authenticatedUserId);

        return createInitialProfileUseCase.execute(authenticatedUserId, onboardingDto);
    }

    @PutMapping("/profile")
    @Operation(
            summary = "Atualizar perfil do usuário - RECOMENDADO",
            description = "Atualiza o perfil para o usuário autenticado. O userId é derivado da autenticação."
    )
    @ApiResponse(responseCode = "200", description = "Perfil atualizado com sucesso")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "404", description = "Perfil não encontrado")
    @ApiResponse(responseCode = "422", description = "Dados de entrada inválidos ou pace incompatível com nível de condicionamento")
    public UserProfileResponseDto updateProfileForAuthenticatedUser(
            @Valid @RequestBody UserProfileUpdateDto updateDto) {

        Long authenticatedUserId = authenticatedUserService.getCurrentUserId();
        log.info("[UserProfileController] Atualizando perfil para usuário autenticado: {}", authenticatedUserId);

        return updateUserProfileUseCase.execute(authenticatedUserId, updateDto);
    }

    @GetMapping("/profile")
    @Operation(
            summary = "Recuperar perfil do usuário - RECOMENDADO",
            description = "Retorna o perfil completo para o usuário autenticado. O userId é derivado da autenticação."
    )
    @ApiResponse(responseCode = "200", description = "Perfil recuperado com sucesso")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "404", description = "Perfil não encontrado")
    public UserProfileResponseDto getProfileForAuthenticatedUser() {
        Long authenticatedUserId = authenticatedUserService.getCurrentUserId();
        log.debug("[UserProfileController] Recuperando perfil para usuário autenticado: {}", authenticatedUserId);

        return getUserProfileUseCase.execute(authenticatedUserId);
    }
}
