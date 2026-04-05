package com.wellness.ritmo.domain.service;

import com.wellness.ritmo.domain.model.Activity;
import com.wellness.ritmo.domain.model.Goal;
import com.wellness.ritmo.domain.service.strategy.GoalEvaluationStrategy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class GoalEvaluationService {

    private final Map<com.wellness.ritmo.domain.model.GoalType, GoalEvaluationStrategy> strategies;

    public GoalEvaluationService(List<GoalEvaluationStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(GoalEvaluationStrategy::supports, Function.identity()));
    }

    public GoalEvaluationStrategy.EvaluationResult evaluate(Activity activity, Goal goal) {
        GoalEvaluationStrategy strategy = strategies.get(goal.getGoalType());
        if (strategy == null) {
            throw new IllegalArgumentException("Nenhuma strategy registrada para GoalType: " + goal.getGoalType());
        }
        return strategy.evaluate(activity, goal);
    }
}
