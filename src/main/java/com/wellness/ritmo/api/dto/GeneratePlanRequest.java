package com.wellness.ritmo.api.dto;

import jakarta.validation.constraints.NotNull;

public record GeneratePlanRequest(
        @NotNull(message = "goalId é obrigatório")
        Long goalId
) {}
