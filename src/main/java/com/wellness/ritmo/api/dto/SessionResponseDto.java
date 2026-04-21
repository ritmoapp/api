package com.wellness.ritmo.api.dto;

import com.wellness.ritmo.domain.model.Enum.SessionStatus;
import com.wellness.ritmo.domain.model.Enum.SessionType;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

public record SessionResponseDto(
        @NotNull Long id,
        @NotNull SessionType sessionType,
        @NotNull String pace,
        @NotNull DayOfWeek dayOfWeek,
        @NotNull SessionStatus status,
        LocalDateTime completedAt
) {}