package com.wellness.ritmo.api.dto;

import com.wellness.ritmo.domain.model.Enum.IntensityPreference;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AvailabilityRequestDto {

    @NotNull(message = "Dia da semana é obrigatório")
    private DayOfWeek dayOfWeek;

    @NotNull(message = "Horário de início é obrigatório")
    private LocalTime startTime;

    @NotNull(message = "Horário de término é obrigatório")
    private LocalTime endTime;

    @Min(value = 20, message = "Sessão mínima: 20 minutos")
    @Max(value = 240, message = "Sessão máxima: 4 horas (240 minutos)")
    private Integer maxSessionMinutes;

    private IntensityPreference preferredIntensity;

    @NotNull(message = "Data de início da disponibilidade é obrigatória")
    private LocalDate validFrom;

    private LocalDate validUntil;
}
