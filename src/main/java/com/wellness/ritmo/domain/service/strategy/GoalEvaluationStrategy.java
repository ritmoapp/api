package com.wellness.ritmo.domain.service.strategy;

import com.wellness.ritmo.domain.model.Activity;
import com.wellness.ritmo.domain.model.Goal;
import com.wellness.ritmo.domain.model.GoalType;

public interface GoalEvaluationStrategy {

    GoalType supports();

    EvaluationResult evaluate(Activity activity, Goal goal);

    record EvaluationResult(boolean achieved, String summary, double delta) {}
}
