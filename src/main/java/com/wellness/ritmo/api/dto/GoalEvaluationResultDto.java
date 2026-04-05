package com.wellness.ritmo.api.dto;

public record GoalEvaluationResultDto(
        boolean achieved,
        String summary,
        double delta
) {}
