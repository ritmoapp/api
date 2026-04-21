package com.wellness.ritmo.api.dto;

import com.wellness.ritmo.domain.model.Enum.SessionStatus;
import com.wellness.ritmo.domain.model.Enum.SessionType;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

public record TrainingSessionResponse(
        Long id,
        SessionType sessionType,
        Integer targetPaceSec,
        DayOfWeek dayOfWeek,
        SessionStatus status,
        LocalDateTime completedAt
) {}
