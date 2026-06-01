package com.wellness.ritmo.api.dto;

import com.wellness.ritmo.domain.model.Enum.GoalStatus;
import com.wellness.ritmo.domain.model.Enum.GoalType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GoalResponseDto {

    private Long id;
    private Long userId;
    private GoalType goalType;
    private GoalStatus status;
    private BigDecimal distanceKm;
    private Integer targetTimeSec;
    private Integer paceTargetSec;
    private Integer weeklyFrequency;
    private Long raceTargetId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
