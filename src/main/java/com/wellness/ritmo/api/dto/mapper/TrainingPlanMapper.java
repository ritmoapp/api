package com.wellness.ritmo.api.dto.mapper;

import com.wellness.ritmo.api.dto.SessionResponseDto;
import com.wellness.ritmo.api.dto.TrainingPlanResponseDto;
import com.wellness.ritmo.domain.model.TrainingPlan;
import com.wellness.ritmo.domain.model.TrainingSession;

import java.time.temporal.IsoFields;
import java.util.List;

public class TrainingPlanMapper {

    private TrainingPlanMapper() {}

    public static TrainingPlanResponseDto toResponse(TrainingPlan plan) {
        List<SessionResponseDto> sessions = plan.getSessions().stream()
                .map(TrainingPlanMapper::toSessionResponse)
                .toList();

        return new TrainingPlanResponseDto(
                plan.getWeekStart().get(IsoFields.WEEK_OF_WEEK_BASED_YEAR),
                plan.getWeekStart(),
                sessions
        );
    }

    public static SessionResponseDto toSessionResponse(TrainingSession session) {
        return new SessionResponseDto(
                session.getId(),
                session.getSessionType(),
                formatPace(session.getTargetPaceSec()),
                session.getDayOfWeek(),
                session.getStatus(),
                session.getCompletedAt()
        );
    }

    public static String formatPace(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
