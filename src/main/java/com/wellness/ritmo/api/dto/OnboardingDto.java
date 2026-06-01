package com.wellness.ritmo.api.dto;

import com.wellness.ritmo.domain.model.Enum.ConditioningLevel;
import com.wellness.ritmo.domain.model.Enum.Gender;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OnboardingDto {

    @NotNull(message = "Gênero é obrigatório")
    private Gender gender;

    @Past(message = "Data de nascimento deve estar no passado")
    private LocalDate birthDate;

    @Min(value = 100, message = "Altura deve ser no mínimo 100 cm")
    @Max(value = 250, message = "Altura deve ser no máximo 250 cm")
    private Integer heightCm;

    @DecimalMin(value = "30.0", message = "Peso deve ser no mínimo 30 kg")
    @DecimalMax(value = "300.0", message = "Peso deve ser no máximo 300 kg")
    private Double weightKg;

    @NotNull(message = "Nível de condicionamento é obrigatório")
    private ConditioningLevel conditioningLevel;

    @NotNull(message = "Pace médio é obrigatório")
    @Min(value = 150, message = "Pace inválido: muito rápido (mínimo 2:30/km)")
    @Max(value = 600, message = "Pace inválido: muito lento (máximo 10:00/km)")
    private Integer paceAvgSeg;

    @DecimalMin(value = "0.0", message = "Quilometragem semanal não pode ser negativa")
    @DecimalMax(value = "300.0", message = "Quilometragem semanal muito alta")
    private Double weeklyMileageKm;
}
