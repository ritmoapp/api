package com.wellness.ritmo.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wellness.ritmo.api.dto.ActivityRequestDto;
import com.wellness.ritmo.api.dto.ActivityResponseDto;
import com.wellness.ritmo.domain.service.ActivityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ActivityController.class)
class ActivityControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ActivityService activityService;

    private ActivityRequestDto validActivityRequest;
    private ActivityResponseDto activityResponse;

    @BeforeEach
    void setUp() {
        validActivityRequest = new ActivityRequestDto(
                new BigDecimal("5.0"),
                1800,
                360,
                145,
                165,
                12,
                LocalDateTime.of(2026, 4, 15, 8, 0),
                LocalDateTime.of(2026, 4, 15, 8, 30),
                "Treino matinal",
                null
        );

        activityResponse = new ActivityResponseDto(
                1L,
                new BigDecimal("5.0"),
                1800,
                360,
                LocalDateTime.of(2026, 4, 15, 8, 0),
                LocalDateTime.of(2026, 4, 15, 8, 30),
                null,
                "O feedback do seu treinador IA está sendo processado e será salvo em breve."
        );
    }

    @Test
    @DisplayName("POST /{userId}/activities - deve criar atividade com sucesso quando userId corresponde ao usuário autenticado")
    void shouldCreateActivityWhenUserIdMatchesAuthenticatedUser() throws Exception {
        Long authenticatedUserId = 1L;
        
        when(activityService.register(eq(authenticatedUserId), any(ActivityRequestDto.class)))
                .thenReturn(activityResponse);

        mockMvc.perform(post("/{userId}/activities", authenticatedUserId)
                        .with(createAuthenticatedUser(authenticatedUserId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validActivityRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.distanceKm").value(5.0))
                .andExpect(jsonPath("$.durationSec").value(1800));

        verify(activityService, times(1)).register(eq(authenticatedUserId), any(ActivityRequestDto.class));
    }

    @Test
    @DisplayName("POST /{userId}/activities - deve retornar 403 quando userId NÃO corresponde ao usuário autenticado (IDOR)")
    void shouldReturn403WhenUserIdDoesNotMatchAuthenticatedUser() throws Exception {
        Long authenticatedUserId = 1L;
        Long targetUserId = 2L;

        mockMvc.perform(post("/{userId}/activities", targetUserId)
                        .with(createAuthenticatedUser(authenticatedUserId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validActivityRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Acesso negado: você não tem permissão para acessar este recurso"));

        verify(activityService, never()).register(any(), any());
    }

    @Test
    @DisplayName("POST /{userId}/activities - deve retornar 403 quando usuário não está autenticado")
    void shouldReturn403WhenUserIsNotAuthenticated() throws Exception {
        Long targetUserId = 1L;

        mockMvc.perform(post("/{userId}/activities", targetUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validActivityRequest)))
                .andExpect(status().isForbidden());

        verify(activityService, never()).register(any(), any());
    }

    @Test
    @DisplayName("POST /{userId}/activities - deve retornar 422 quando dados são inválidos")
    void shouldReturn422WhenActivityDataIsInvalid() throws Exception {
        Long authenticatedUserId = 1L;
        
        ActivityRequestDto invalidRequest = new ActivityRequestDto(
                new BigDecimal("0.0"),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        mockMvc.perform(post("/{userId}/activities", authenticatedUserId)
                        .with(createAuthenticatedUser(authenticatedUserId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isUnprocessableEntity());

        verify(activityService, never()).register(any(), any());
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor createAuthenticatedUser(Long userId) {
        CustomUserPrincipal principal = new CustomUserPrincipal(userId, "user" + userId);
        return user(principal);
    }

    private static class CustomUserPrincipal implements org.springframework.security.core.userdetails.UserDetails {
        private final Long id;
        private final String username;

        public CustomUserPrincipal(Long id, String username) {
            this.id = id;
            this.username = username;
        }

        public Long getId() {
            return id;
        }

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public String getPassword() {
            return "password";
        }

        @Override
        public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }
}
