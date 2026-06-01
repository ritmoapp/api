package com.wellness.ritmo.api.dto;

import com.wellness.ritmo.domain.model.Enum.GoalType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GoalRequestDto {

    @NotNull(message = "Tipo de objetivo é obrigatório")
    private GoalType goalType;

    @DecimalMin(value = "0.5", message = "Distância mínima: 0.5 km")
    @DecimalMax(value = "200.0", message = "Distância máxima: 200 km")
    private BigDecimal distanceKm;

    @Min(value = 60, message = "Tempo alvo mínimo: 1 minuto (60 segundos)")
    @Max(value = 86400, message = "Tempo alvo máximo: 24 horas")
    private Integer targetTimeSec;

    @Min(value = 150, message = "Pace alvo inválido: muito rápido (mínimo 2:30/km)")
    @Max(value = 600, message = "Pace alvo inválido: muito lento (máximo 10:00/km)")
    private Integer paceTargetSec;

    @Min(value = 3, message = "Frequência semanal mínima: 3 dias")
    @Max(value = 6, message = "Frequência semanal máxima: 6 dias")
    private Integer weeklyFrequency;

    private Long raceTargetId;
}
