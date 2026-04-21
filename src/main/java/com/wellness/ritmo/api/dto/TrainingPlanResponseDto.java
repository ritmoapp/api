package com.wellness.ritmo.api.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record TrainingPlanResponseDto(
        @NotNull Integer week,
        @NotNull LocalDate startsOn,
        @NotNull List<SessionResponseDto> sessions
) {}
