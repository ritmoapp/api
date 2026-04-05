package com.wellness.ritmo.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ActivityResponseDto(
        Long id,
        BigDecimal distanceKm,
        Integer durationSec,
        Integer paceAvgSec,
        LocalDateTime startedAt,
        LocalDateTime finishedAt,
        GoalEvaluationResultDto goalEvaluation,
        String aiFeedbackStatus
) {}
