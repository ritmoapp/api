package com.wellness.ritmo.domain.service.strategy;

import com.wellness.ritmo.domain.model.Activity;
import com.wellness.ritmo.domain.model.Goal;
import com.wellness.ritmo.domain.model.Enum.GoalType;
import org.springframework.stereotype.Component;

@Component
public class DistanceGoalStrategy implements GoalEvaluationStrategy {

    @Override
    public GoalType supports() {
        return GoalType.DISTANCE;
    }

    @Override
    public EvaluationResult evaluate(Activity activity, Goal goal) {
        double delta = activity.getDistanceKm().doubleValue() - goal.getDistanceKm().doubleValue();
        boolean achieved = delta >= 0;
        String summary = achieved
                ? String.format("%.2f km além da distância alvo. Meta batida!", delta)
                : String.format("%.2f km abaixo da distância alvo. Continue evoluindo.", Math.abs(delta));
        return new EvaluationResult(achieved, summary, delta);
    }
}
