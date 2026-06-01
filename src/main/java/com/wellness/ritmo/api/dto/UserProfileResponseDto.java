package com.wellness.ritmo.api.dto;

import com.wellness.ritmo.domain.model.Enum.ConditioningLevel;
import com.wellness.ritmo.domain.model.Enum.Gender;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserProfileResponseDto {

    private Long id;
    private Long userId;
    private Gender gender;
    private LocalDate birthDate;
    private Integer heightCm;
    private Double weightKg;
    private ConditioningLevel conditioningLevel;
    private Integer paceAvgSeg;
    private Double weeklyMileageKm;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
