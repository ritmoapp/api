package com.wellness.ritmo.api.controller;

import com.wellness.ritmo.api.dto.OnboardingDto;
import com.wellness.ritmo.api.dto.UserProfileResponseDto;
import com.wellness.ritmo.api.dto.mapper.UserProfileMapper;
import com.wellness.ritmo.domain.model.UserProfile;
import com.wellness.ritmo.domain.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/{userId}/profile")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "Gerenciamento de perfil do usuário")
public class UserProfileController {

    private final UserProfileService userProfileService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Criar perfil inicial do usuário (onboarding)",
            description = "Cria o perfil inicial do usuário com dados de onboarding"
    )
    @ApiResponse(responseCode = "201", description = "Perfil criado com sucesso")
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    @ApiResponse(responseCode = "409", description = "Usuário já possui um perfil")
    @ApiResponse(responseCode = "422", description = "Dados de entrada inválidos ou pace incompatível com nível de condicionamento")
    public UserProfileResponseDto createProfile(
            @PathVariable Long userId,
            @Valid @RequestBody OnboardingDto onboardingDto) {

        UserProfile profile = userProfileService.createInitialProfile(userId, onboardingDto);
        return UserProfileMapper.toDto(profile);
    }

    @PutMapping
    @Operation(
            summary = "Atualizar perfil do usuário",
            description = "Atualiza os dados do perfil do usuário"
    )
    @ApiResponse(responseCode = "200", description = "Perfil atualizado com sucesso")
    @ApiResponse(responseCode = "404", description = "Perfil não encontrado")
    @ApiResponse(responseCode = "422", description = "Dados de entrada inválidos ou pace incompatível com nível de condicionamento")
    public UserProfileResponseDto updateProfile(
            @PathVariable Long userId,
            @Valid @RequestBody OnboardingDto onboardingDto) {

        UserProfile profile = userProfileService.updateProfile(userId, onboardingDto);
        return UserProfileMapper.toDto(profile);
    }

    @GetMapping
    @Operation(
            summary = "Recuperar perfil do usuário",
            description = "Retorna o perfil completo do usuário"
    )
    @ApiResponse(responseCode = "200", description = "Perfil recuperado com sucesso")
    @ApiResponse(responseCode = "404", description = "Perfil não encontrado")
    public UserProfileResponseDto getProfile(@PathVariable Long userId) {
        UserProfile profile = userProfileService.getProfileByUserId(userId);
        return UserProfileMapper.toDto(profile);
    }
}
