package com.wellness.ritmo.api.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ActivityRequestDto(

        @NotNull(message = "distanceKm é obrigatório")
        @DecimalMin(value = "0.01", message = "distanceKm deve ser maior que 0")
        BigDecimal distanceKm,

        @NotNull(message = "durationSec é obrigatório")
        @Min(value = 1, message = "durationSec deve ser maior que 0")
        Integer durationSec,

        Integer paceAvgSec,
        Integer heartRateAvg,
        Integer heartRateMax,

        @Min(value = 6, message = "Esforço percebido mínimo é 6 (Borg scale)")
        @Max(value = 20, message = "Esforço percebido máximo é 20 (Borg scale)")
        Integer perceivedEffort,

        @NotNull(message = "startedAt é obrigatório")
        LocalDateTime startedAt,

        LocalDateTime finishedAt,
        String notes,
        Long goalId
) {}
