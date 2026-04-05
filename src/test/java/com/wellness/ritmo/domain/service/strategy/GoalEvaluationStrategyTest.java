package com.wellness.ritmo.domain.service.strategy;

import com.wellness.ritmo.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class GoalEvaluationStrategyTest {

    private Activity activity;
    private Goal goal;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(1L);

        activity = new Activity();
        activity.setUser(user);

        goal = new Goal();
        goal.setUser(user);
        goal.setStatus(GoalStatus.OPEN);
    }

    @Nested
    @DisplayName("PaceGoalStrategy")
    class PaceGoalStrategyTests {

        private final PaceGoalStrategy strategy = new PaceGoalStrategy();

        @Test
        @DisplayName("deve retornar achieved=true quando pace realizado é menor ou igual ao alvo")
        void shouldAchieveWhenPaceIsBelowTarget() {
            activity.setPaceAvgSec(295);
            goal.setGoalType(GoalType.PACE);
            goal.setPaceTargetSec(300);

            var result = strategy.evaluate(activity, goal);

            assertThat(result.achieved()).isTrue();
            assertThat(result.delta()).isEqualTo(-5.0);
            assertThat(result.summary()).contains("Meta batida");
        }

        @Test
        @DisplayName("deve retornar achieved=false quando pace realizado é maior que o alvo")
        void shouldNotAchieveWhenPaceIsAboveTarget() {
            activity.setPaceAvgSec(310);
            goal.setGoalType(GoalType.PACE);
            goal.setPaceTargetSec(300);

            var result = strategy.evaluate(activity, goal);

            assertThat(result.achieved()).isFalse();
            assertThat(result.delta()).isEqualTo(10.0);
            assertThat(result.summary()).contains("Continue evoluindo");
        }

        @Test
        @DisplayName("deve retornar achieved=true quando pace é exatamente igual ao alvo")
        void shouldAchieveWhenPaceEqualsTarget() {
            activity.setPaceAvgSec(300);
            goal.setGoalType(GoalType.PACE);
            goal.setPaceTargetSec(300);

            var result = strategy.evaluate(activity, goal);

            assertThat(result.achieved()).isTrue();
            assertThat(result.delta()).isZero();
        }
    }

    @Nested
    @DisplayName("DistanceGoalStrategy")
    class DistanceGoalStrategyTests {

        private final DistanceGoalStrategy strategy = new DistanceGoalStrategy();

        @Test
        @DisplayName("deve retornar achieved=true quando distância realizada é maior ou igual ao alvo")
        void shouldAchieveWhenDistanceMeetsTarget() {
            activity.setDistanceKm(new BigDecimal("10.5"));
            goal.setGoalType(GoalType.DISTANCE);
            goal.setDistanceKm(new BigDecimal("10.0"));

            var result = strategy.evaluate(activity, goal);

            assertThat(result.achieved()).isTrue();
            assertThat(result.delta()).isEqualTo(0.5);
        }

        @Test
        @DisplayName("deve retornar achieved=false quando distância realizada é menor que o alvo")
        void shouldNotAchieveWhenDistanceBelowTarget() {
            activity.setDistanceKm(new BigDecimal("8.0"));
            goal.setGoalType(GoalType.DISTANCE);
            goal.setDistanceKm(new BigDecimal("10.0"));

            var result = strategy.evaluate(activity, goal);

            assertThat(result.achieved()).isFalse();
            assertThat(result.delta()).isEqualTo(-2.0);
        }
    }

    @Nested
    @DisplayName("TimeGoalStrategy")
    class TimeGoalStrategyTests {

        private final TimeGoalStrategy strategy = new TimeGoalStrategy();

        @Test
        @DisplayName("deve retornar achieved=true quando duração é menor ou igual ao tempo alvo")
        void shouldAchieveWhenDurationBelowTarget() {
            activity.setDurationSec(2900);
            goal.setGoalType(GoalType.TIME);
            goal.setTargetTimeSec(3000);

            var result = strategy.evaluate(activity, goal);

            assertThat(result.achieved()).isTrue();
            assertThat(result.delta()).isEqualTo(-100.0);
        }

        @Test
        @DisplayName("deve retornar achieved=false quando duração excede o tempo alvo")
        void shouldNotAchieveWhenDurationAboveTarget() {
            activity.setDurationSec(3120);
            goal.setGoalType(GoalType.TIME);
            goal.setTargetTimeSec(3000);

            var result = strategy.evaluate(activity, goal);

            assertThat(result.achieved()).isFalse();
            assertThat(result.delta()).isEqualTo(120.0);
        }
    }
}
