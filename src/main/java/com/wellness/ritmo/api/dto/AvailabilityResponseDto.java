package com.wellness.ritmo.api.dto;

import com.wellness.ritmo.domain.model.Enum.IntensityPreference;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AvailabilityResponseDto {

    private Long id;
    private Long userId;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer maxSessionMinutes;
    private IntensityPreference preferredIntensity;
    private LocalDate validFrom;
    private LocalDate validUntil;
}
