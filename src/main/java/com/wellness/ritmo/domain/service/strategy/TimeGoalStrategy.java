package com.wellness.ritmo.domain.service.strategy;

import com.wellness.ritmo.domain.model.Activity;
import com.wellness.ritmo.domain.model.Goal;
import com.wellness.ritmo.domain.model.Enum.GoalType;
import org.springframework.stereotype.Component;

@Component
public class TimeGoalStrategy implements GoalEvaluationStrategy {

    @Override
    public GoalType supports() {
        return GoalType.TIME;
    }

    @Override
    public EvaluationResult evaluate(Activity activity, Goal goal) {
        double delta = activity.getDurationSec() - goal.getTargetTimeSec();
        boolean achieved = delta <= 0;
        String summary = achieved
                ? String.format("%.0f seg abaixo do tempo alvo. Meta batida!", Math.abs(delta))
                : String.format("%.0f seg acima do tempo alvo. Continue evoluindo.", delta);
        return new EvaluationResult(achieved, summary, delta);
    }
}
