package com.wellness.ritmo.api.dto.mapper;

import com.wellness.ritmo.api.dto.GoalResponseDto;
import com.wellness.ritmo.domain.model.Goal;

public class GoalMapper {

    private GoalMapper() {
    }

    public static GoalResponseDto toDto(Goal goal) {
        GoalResponseDto dto = new GoalResponseDto();
        dto.setId(goal.getId());
        dto.setUserId(goal.getUser().getId());
        dto.setGoalType(goal.getGoalType());
        dto.setStatus(goal.getStatus());
        dto.setDistanceKm(goal.getDistanceKm());
        dto.setTargetTimeSec(goal.getTargetTimeSec());
        dto.setPaceTargetSec(goal.getPaceTargetSec());
        dto.setWeeklyFrequency(goal.getWeeklyFrequency());
        dto.setRaceTargetId(goal.getRaceTarget() != null ? goal.getRaceTarget().getId() : null);
        dto.setCreatedAt(goal.getCreatedAt());
        dto.setUpdatedAt(goal.getUpdatedAt());
        return dto;
    }
}
