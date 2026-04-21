package com.wellness.ritmo.api.dto.mapper;

import com.wellness.ritmo.api.dto.TrainingPlanResponse;
import com.wellness.ritmo.api.dto.TrainingSessionResponse;
import com.wellness.ritmo.domain.model.TrainingPlan;
import com.wellness.ritmo.domain.model.TrainingSession;

import java.util.List;

public class TrainingPlanMapper {

    private TrainingPlanMapper() {}

    public static TrainingPlanResponse toResponse(TrainingPlan plan) {
        List<TrainingSessionResponse> sessions = plan.getSessions().stream()
                .map(TrainingPlanMapper::toSessionResponse)
                .toList();

        return new TrainingPlanResponse(
                plan.getId(),
                plan.getUser().getId(),
                plan.getGoal().getId(),
                plan.getActive(),
                plan.getWeekStart(),
                plan.getCreatedAt(),
                sessions
        );
    }

    public static TrainingSessionResponse toSessionResponse(TrainingSession session) {
        return new TrainingSessionResponse(
                session.getId(),
                session.getSessionType(),
                session.getTargetPaceSec(),
                session.getDayOfWeek(),
                session.getStatus(),
                session.getCompletedAt()
        );
    }
}
