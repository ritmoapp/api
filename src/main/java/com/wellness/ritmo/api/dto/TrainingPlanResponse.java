package com.wellness.ritmo.api.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record TrainingPlanResponse(
        Long id,
        Long userId,
        Long goalId,
        Boolean active,
        LocalDate weekStart,
        LocalDateTime createdAt,
        List<TrainingSessionResponse> sessions
) {}
