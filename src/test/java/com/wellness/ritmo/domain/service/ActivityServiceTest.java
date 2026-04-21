package com.wellness.ritmo.domain.service;

import com.wellness.ritmo.api.dto.ActivityRequestDto;
import com.wellness.ritmo.api.dto.ActivityResponseDto;
import com.wellness.ritmo.domain.model.*;
import com.wellness.ritmo.domain.model.Enum.GoalStatus;
import com.wellness.ritmo.domain.model.Enum.GoalType;
import com.wellness.ritmo.domain.repository.ActivityRepository;
import com.wellness.ritmo.domain.repository.GoalRepository;
import com.wellness.ritmo.domain.repository.UserRepository;
import com.wellness.ritmo.domain.service.strategy.*;
import com.wellness.ritmo.infrastructure.ai.AICoachService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock private ActivityRepository activityRepository;
    @Mock private GoalRepository goalRepository;
    @Mock private UserRepository userRepository;
    @Mock private AICoachService aiCoachService;

    @Spy
    private GoalEvaluationService goalEvaluationService = new GoalEvaluationService(
            List.of(new PaceGoalStrategy(), new DistanceGoalStrategy(), new TimeGoalStrategy())
    );

    @InjectMocks
    private ActivityService activityService;

    private User user;
    private Goal goal;
    private ActivityRequestDto requestDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        goal = new Goal();
        goal.setId(42L);
        goal.setUser(user);
        goal.setGoalType(GoalType.PACE);
        goal.setPaceTargetSec(300);
        goal.setStatus(GoalStatus.OPEN);

        requestDto = new ActivityRequestDto(
                new BigDecimal("5.0"),
                1500,
                295,
                155,
                175,
                14,
                LocalDateTime.now().minusMinutes(30),
                LocalDateTime.now(),
                "Treino leve matinal",
                42L
        );
    }

    @Test
    @DisplayName("deve salvar Activity, avaliar meta e marcar GoalStatus como COMPLETED quando meta é batida")
    void shouldRegisterActivityAndCompleteGoalWhenAchieved() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(goalRepository.findByIdAndUserIdAndStatus(42L, 1L, GoalStatus.OPEN)).thenReturn(Optional.of(goal));
        when(activityRepository.save(any(Activity.class))).thenAnswer(inv -> {
            Activity a = inv.getArgument(0);
            a.setId(99L);
            return a;
        });
        doNothing().when(aiCoachService).generateFeedbackAsync(any());

        ActivityResponseDto response = activityService.register(1L, requestDto);

        assertThat(response.id()).isEqualTo(99L);
        assertThat(response.goalEvaluation()).isNotNull();
        assertThat(response.goalEvaluation().achieved()).isTrue();
        assertThat(response.goalEvaluation().delta()).isEqualTo(-5.0);
        assertThat(response.aiFeedbackStatus()).contains("sendo processado");

        verify(goalRepository).updateStatus(42L, GoalStatus.COMPLETED);
        verify(aiCoachService).generateFeedbackAsync(any(Activity.class));
    }

    @Test
    @DisplayName("não deve chamar updateStatus quando meta não é batida")
    void shouldNotCompleteGoalWhenNotAchieved() {
        ActivityRequestDto slowDto = new ActivityRequestDto(
                new BigDecimal("5.0"), 1800, 320, null, null, null,
                LocalDateTime.now().minusMinutes(30), LocalDateTime.now(), null, 42L
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(goalRepository.findByIdAndUserIdAndStatus(42L, 1L, GoalStatus.OPEN)).thenReturn(Optional.of(goal));
        when(activityRepository.save(any())).thenAnswer(inv -> {
            Activity a = inv.getArgument(0);
            a.setId(100L);
            return a;
        });
        doNothing().when(aiCoachService).generateFeedbackAsync(any());

        ActivityResponseDto response = activityService.register(1L, slowDto);

        assertThat(response.goalEvaluation().achieved()).isFalse();
        assertThat(response.goalEvaluation().delta()).isEqualTo(20.0);
        verify(goalRepository, never()).updateStatus(any(), any());
    }

    @Test
    @DisplayName("deve salvar Activity sem meta quando goalId é nulo")
    void shouldRegisterActivityWithoutGoal() {
        ActivityRequestDto noGoalDto = new ActivityRequestDto(
                new BigDecimal("3.0"), 900, 280, null, null, null,
                LocalDateTime.now().minusMinutes(15), LocalDateTime.now(), null, null
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(activityRepository.save(any())).thenAnswer(inv -> {
            Activity a = inv.getArgument(0);
            a.setId(101L);
            return a;
        });
        doNothing().when(aiCoachService).generateFeedbackAsync(any());

        ActivityResponseDto response = activityService.register(1L, noGoalDto);

        assertThat(response.goalEvaluation()).isNull();
        verify(goalRepository, never()).findByIdAndUserIdAndStatus(any(), any(), any());
    }

    @Test
    @DisplayName("deve lançar exceção quando usuário não existe")
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> activityService.register(99L, requestDto))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class)
                .hasMessageContaining("Usuário não encontrado");
    }

    @Test
    @DisplayName("deve lançar exceção quando meta não pertence ao usuário ou não está OPEN")
    void shouldThrowWhenGoalNotFoundOrNotOpen() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(goalRepository.findByIdAndUserIdAndStatus(42L, 1L, GoalStatus.OPEN))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> activityService.register(1L, requestDto))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class)
                .hasMessageContaining("Meta não encontrada");
    }
}
