package com.wellness.ritmo.domain.service;

import com.wellness.ritmo.domain.model.*;
import com.wellness.ritmo.domain.model.Enum.GoalStatus;
import com.wellness.ritmo.domain.model.Enum.GoalType;
import com.wellness.ritmo.domain.model.Enum.SessionType;
import com.wellness.ritmo.domain.repository.GoalRepository;
import com.wellness.ritmo.domain.repository.TrainingPlanRepository;
import com.wellness.ritmo.domain.repository.UserAvailabilityRepository;
import com.wellness.ritmo.domain.repository.UserProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.wellness.ritmo.domain.service.strategy.EasyPaceStrategy;
import com.wellness.ritmo.domain.service.strategy.IntervalsPaceStrategy;
import com.wellness.ritmo.domain.service.strategy.LongRunPaceStrategy;

import java.time.DayOfWeek;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingPlanServiceTest {

    @Mock private GoalRepository goalRepository;
    @Mock private UserAvailabilityRepository userAvailabilityRepository;
    @Mock private UserProfileRepository userProfileRepository;
    @Mock private TrainingPlanRepository trainingPlanRepository;

    private TrainingPlanService trainingPlanService;

    private User user;
    private Goal goal;
    private UserProfile profile;

    private static final Long USER_ID = 1L;
    private static final Long GOAL_ID = 10L;
    private static final int PACE_AVG = 300;

    @BeforeEach
    void setUp() {
        trainingPlanService = new TrainingPlanService(
            goalRepository,
            userAvailabilityRepository,
            userProfileRepository,
            trainingPlanRepository,
            List.of(
                new EasyPaceStrategy(),
                new IntervalsPaceStrategy(),
                new LongRunPaceStrategy()
            )
        );

        user = new User();
        user.setId(USER_ID);

        profile = new UserProfile();
        profile.setId(1L);
        profile.setUser(user);
        profile.setPaceAvgSeg(PACE_AVG);

        goal = new Goal();
        goal.setId(GOAL_ID);
        goal.setUser(user);
        goal.setGoalType(GoalType.PACE);
        goal.setStatus(GoalStatus.OPEN);
    }

    @Test
    @DisplayName("3x por semana: 1 LONG_RUN, 1 INTERVALS, 1 EASY")
    void shouldGenerate3SessionsCorrectly() {
        goal.setWeeklyFrequency(3);

        List<UserAvailability> availabilities = List.of(
                buildAvailability(DayOfWeek.TUESDAY),
                buildAvailability(DayOfWeek.THURSDAY),
                buildAvailability(DayOfWeek.SATURDAY)
        );

        mockDependencies(availabilities);

        TrainingPlan result = trainingPlanService.generatePlan(USER_ID, GOAL_ID);

        assertThat(result.getSessions()).hasSize(3);
        assertSessionTypeCount(result, SessionType.LONG_RUN, 1);
        assertSessionTypeCount(result, SessionType.INTERVALS, 1);
        assertSessionTypeCount(result, SessionType.EASY, 1);
        assertPaceCalculations(result);
        verify(trainingPlanRepository).save(any(TrainingPlan.class));
    }

    @Test
    @DisplayName("4x por semana: 1 LONG_RUN, 1 INTERVALS, 2 EASY")
    void shouldGenerate4SessionsCorrectly() {
        goal.setWeeklyFrequency(4);

        List<UserAvailability> availabilities = List.of(
                buildAvailability(DayOfWeek.MONDAY),
                buildAvailability(DayOfWeek.WEDNESDAY),
                buildAvailability(DayOfWeek.FRIDAY),
                buildAvailability(DayOfWeek.SUNDAY)
        );

        mockDependencies(availabilities);

        TrainingPlan result = trainingPlanService.generatePlan(USER_ID, GOAL_ID);

        assertThat(result.getSessions()).hasSize(4);
        assertSessionTypeCount(result, SessionType.LONG_RUN, 1);
        assertSessionTypeCount(result, SessionType.INTERVALS, 1);
        assertSessionTypeCount(result, SessionType.EASY, 2);
        assertPaceCalculations(result);
        verify(trainingPlanRepository).save(any(TrainingPlan.class));
    }

    @Test
    @DisplayName("5x por semana: 1 LONG_RUN, 1 INTERVALS, 3 EASY")
    void shouldGenerate5SessionsCorrectly() {
        goal.setWeeklyFrequency(5);

        List<UserAvailability> availabilities = List.of(
                buildAvailability(DayOfWeek.MONDAY),
                buildAvailability(DayOfWeek.TUESDAY),
                buildAvailability(DayOfWeek.WEDNESDAY),
                buildAvailability(DayOfWeek.FRIDAY),
                buildAvailability(DayOfWeek.SATURDAY)
        );

        mockDependencies(availabilities);

        TrainingPlan result = trainingPlanService.generatePlan(USER_ID, GOAL_ID);

        assertThat(result.getSessions()).hasSize(5);
        assertSessionTypeCount(result, SessionType.LONG_RUN, 1);
        assertSessionTypeCount(result, SessionType.INTERVALS, 1);
        assertSessionTypeCount(result, SessionType.EASY, 3);
        assertPaceCalculations(result);
        verify(trainingPlanRepository).save(any(TrainingPlan.class));
    }

    @Test
    @DisplayName("Sem UserAvailability: lança IllegalStateException")
    void shouldThrowIllegalStateWhenNoAvailability() {
        goal.setWeeklyFrequency(3);

        when(goalRepository.findByIdAndUserIdAndStatus(GOAL_ID, USER_ID, GoalStatus.OPEN))
                .thenReturn(Optional.of(goal));
        when(userAvailabilityRepository.findValidByUserId(USER_ID))
                .thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> trainingPlanService.generatePlan(USER_ID, GOAL_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Nenhuma disponibilidade válida encontrada para o usuário " + USER_ID);

        verify(trainingPlanRepository, never()).save(any());
    }

    @Test
    @DisplayName("Goal de outro usuário: lança EntityNotFoundException")
    void shouldThrowEntityNotFoundWhenGoalBelongsToAnotherUser() {
        Long otherUserId = 999L;

        when(goalRepository.findByIdAndUserIdAndStatus(GOAL_ID, otherUserId, GoalStatus.OPEN))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingPlanService.generatePlan(otherUserId, GOAL_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Goal " + GOAL_ID + " não encontrado para o usuário " + otherUserId);

        verify(trainingPlanRepository, never()).save(any());
    }

    private void mockDependencies(List<UserAvailability> availabilities) {
        when(goalRepository.findByIdAndUserIdAndStatus(GOAL_ID, USER_ID, GoalStatus.OPEN))
                .thenReturn(Optional.of(goal));
        when(userAvailabilityRepository.findValidByUserId(USER_ID))
                .thenReturn(availabilities);
        when(userProfileRepository.findByUserId(USER_ID))
                .thenReturn(Optional.of(profile));
        when(trainingPlanRepository.save(any(TrainingPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void assertSessionTypeCount(TrainingPlan plan, SessionType type, int expectedCount) {
        long count = plan.getSessions().stream()
                .filter(s -> s.getSessionType() == type)
                .count();
        assertThat(count).as("Expected %d sessions of type %s", expectedCount, type)
                .isEqualTo(expectedCount);
    }

    private void assertPaceCalculations(TrainingPlan plan) {
        plan.getSessions().forEach(session -> {
            switch (session.getSessionType()) {
                case EASY -> assertThat(session.getTargetPaceSec())
                        .isEqualTo((int) Math.round(PACE_AVG * 1.2));
                case INTERVALS -> assertThat(session.getTargetPaceSec())
                        .isEqualTo((int) Math.round(PACE_AVG * 0.9));
                case LONG_RUN -> assertThat(session.getTargetPaceSec())
                        .isEqualTo((int) Math.round(PACE_AVG * 1.1));
            }
        });
    }

    private UserAvailability buildAvailability(DayOfWeek dayOfWeek) {
        UserAvailability ua = new UserAvailability();
        ua.setUser(user);
        ua.setDayOfWeek(dayOfWeek);
        return ua;
    }
}
