package com.wellness.ritmo.domain.service.strategy;

import com.wellness.ritmo.domain.model.Activity;
import com.wellness.ritmo.domain.model.Goal;
import com.wellness.ritmo.domain.model.Enum.GoalType;
import org.springframework.stereotype.Component;

@Component
public class PaceGoalStrategy implements GoalEvaluationStrategy {

    @Override
    public GoalType supports() {
        return GoalType.PACE;
    }

    @Override
    public EvaluationResult evaluate(Activity activity, Goal goal) {
        double delta = activity.getPaceAvgSec() - goal.getPaceTargetSec();
        boolean achieved = delta <= 0;
        String summary = achieved
                ? String.format("Pace %.0f seg/km abaixo do alvo. Meta batida!", Math.abs(delta))
                : String.format("Pace %.0f seg/km acima do alvo. Continue evoluindo.", delta);
        return new EvaluationResult(achieved, summary, delta);
    }
}
