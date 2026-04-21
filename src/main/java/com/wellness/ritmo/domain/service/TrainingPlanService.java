package com.wellness.ritmo.domain.service;

import com.wellness.ritmo.domain.model.*;
import com.wellness.ritmo.domain.model.Enum.GoalStatus;
import com.wellness.ritmo.domain.model.Enum.SessionType;
import com.wellness.ritmo.domain.repository.GoalRepository;
import com.wellness.ritmo.domain.repository.TrainingPlanRepository;
import com.wellness.ritmo.domain.repository.UserAvailabilityRepository;
import com.wellness.ritmo.domain.repository.UserProfileRepository;
import com.wellness.ritmo.domain.service.strategy.PaceCalculationStrategy;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TrainingPlanService {

    private static final Set<DayOfWeek> WEEKEND_DAYS = EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
    private static final Set<DayOfWeek> MIDWEEK_DAYS = EnumSet.of(DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY);

    private final GoalRepository goalRepository;
    private final UserAvailabilityRepository userAvailabilityRepository;
    private final UserProfileRepository userProfileRepository;
    private final TrainingPlanRepository trainingPlanRepository;
    private final Map<SessionType, PaceCalculationStrategy> paceStrategies;

    public TrainingPlanService(
            GoalRepository goalRepository,
            UserAvailabilityRepository userAvailabilityRepository,
            UserProfileRepository userProfileRepository,
            TrainingPlanRepository trainingPlanRepository,
            List<PaceCalculationStrategy> strategies) {
        this.goalRepository = goalRepository;
        this.userAvailabilityRepository = userAvailabilityRepository;
        this.userProfileRepository = userProfileRepository;
        this.trainingPlanRepository = trainingPlanRepository;
        this.paceStrategies = strategies.stream()
                .collect(Collectors.toMap(PaceCalculationStrategy::supports, Function.identity()));
    }

    @Transactional
    public TrainingPlan generatePlan(Long userId, Long goalId) {
        log.info("Iniciando geração de plano para userId={} goalId={}", userId, goalId);

        Goal goal = goalRepository.findByIdAndUserIdAndStatus(goalId, userId, GoalStatus.OPEN)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Goal " + goalId + " não encontrado para o usuário " + userId));

        List<UserAvailability> availabilities = userAvailabilityRepository.findValidByUserId(userId);
        if (availabilities.isEmpty()) {
            throw new IllegalStateException(
                    "Nenhuma disponibilidade válida encontrada para o usuário " + userId);
        }

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Perfil não encontrado para o usuário " + userId));

        List<DayOfWeek> selectedDays = selectDays(availabilities, goal.getWeeklyFrequency());

        DayOfWeek longRunDay = selectPreferredDay(selectedDays, WEEKEND_DAYS);
        DayOfWeek intervalsDay = selectPreferredDay(
                selectedDays.stream().filter(d -> !d.equals(longRunDay)).collect(Collectors.toList()),
                MIDWEEK_DAYS);

        int paceAvg = profile.getPaceAvgSeg();

        User user = goal.getUser();
        TrainingPlan plan = new TrainingPlan();
        plan.setUser(user);
        plan.setGoal(goal);

        List<TrainingSession> sessions = new ArrayList<>();
        for (DayOfWeek day : selectedDays) {
            TrainingSession session = new TrainingSession();
            session.setTrainingPlan(plan);
            session.setDayOfWeek(day);

            SessionType type;
            if (day.equals(longRunDay)) {
                type = SessionType.LONG_RUN;
            } else if (day.equals(intervalsDay)) {
                type = SessionType.INTERVALS;
            } else {
                type = SessionType.EASY;
            }

            session.setSessionType(type);
            session.setTargetPaceSec(paceStrategies.get(type).calculatePace(paceAvg));
            sessions.add(session);
        }

        plan.setSessions(sessions);
        TrainingPlan saved = trainingPlanRepository.save(plan);

        log.info("Plano gerado com sucesso para userId={} goalId={} — {} sessões criadas",
                userId, goalId, sessions.size());

        return saved;
    }

    List<DayOfWeek> selectDays(List<UserAvailability> availabilities, int weeklyFrequency) {
        List<DayOfWeek> available = availabilities.stream()
                .map(UserAvailability::getDayOfWeek)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        if (available.size() <= weeklyFrequency) {
            return available;
        }

        List<DayOfWeek> selected = new ArrayList<>();
        double interval = (double) available.size() / weeklyFrequency;
        for (int i = 0; i < weeklyFrequency; i++) {
            int index = (int) Math.round(i * interval);
            if (index >= available.size()) {
                index = available.size() - 1;
            }
            DayOfWeek day = available.get(index);
            if (!selected.contains(day)) {
                selected.add(day);
            }
        }

        if (selected.size() < weeklyFrequency) {
            for (DayOfWeek day : available) {
                if (!selected.contains(day)) {
                    selected.add(day);
                    if (selected.size() == weeklyFrequency) break;
                }
            }
        }

        selected.sort(Comparator.naturalOrder());
        return selected;
    }

    private DayOfWeek selectPreferredDay(List<DayOfWeek> days, Set<DayOfWeek> preferred) {
        return days.stream()
                .filter(preferred::contains)
                .findFirst()
                .orElse(days.get(days.size() - 1));
    }
}
