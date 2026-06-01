package com.wellness.ritmo.domain.service;

import com.wellness.ritmo.api.dto.GoalRequestDto;
import com.wellness.ritmo.domain.model.Enum.GoalStatus;
import com.wellness.ritmo.domain.model.Enum.GoalType;
import com.wellness.ritmo.domain.model.Goal;
import com.wellness.ritmo.domain.model.User;
import com.wellness.ritmo.domain.repository.GoalRepository;
import com.wellness.ritmo.domain.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class GoalService {

    private final GoalRepository goalRepository;
    private final UserRepository userRepository;

    @Transactional
    public Goal create(Long userId, GoalRequestDto dto) {
        log.info("[GoalService] Criando goal para usuário: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: " + userId));

        validateGoalFields(dto);

        Goal goal = new Goal();
        goal.setUser(user);
        goal.setGoalType(dto.getGoalType());
        goal.setStatus(GoalStatus.OPEN);
        goal.setDistanceKm(dto.getDistanceKm());
        goal.setTargetTimeSec(dto.getTargetTimeSec());
        goal.setPaceTargetSec(dto.getPaceTargetSec());
        goal.setWeeklyFrequency(dto.getWeeklyFrequency());

        Goal saved = goalRepository.save(goal);
        log.info("[GoalService] Goal criado com sucesso: {}", saved.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    public Goal getById(Long userId, Long goalId) {
        return goalRepository.findById(goalId)
                .filter(g -> g.getUser().getId().equals(userId))
                .orElseThrow(() -> new EntityNotFoundException(
                        "Goal não encontrado para o usuário: " + userId));
    }

    @Transactional(readOnly = true)
    public List<Goal> getAllByUser(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: " + userId));
        return goalRepository.findAll().stream()
                .filter(g -> g.getUser().getId().equals(userId))
                .toList();
    }

    @Transactional
    public void cancel(Long userId, Long goalId) {
        Goal goal = getById(userId, goalId);
        if (goal.getStatus() == GoalStatus.COMPLETED) {
            throw new IllegalStateException("Não é possível cancelar um goal já completado");
        }
        goalRepository.updateStatus(goalId, GoalStatus.CANCELLED);
        log.info("[GoalService] Goal {} cancelado para usuário: {}", goalId, userId);
    }

    private void validateGoalFields(GoalRequestDto dto) {
        GoalType type = dto.getGoalType();

        if (type == GoalType.DISTANCE && dto.getDistanceKm() == null) {
            throw new IllegalArgumentException("Distância é obrigatória para objetivo do tipo DISTANCE");
        }
        if (type == GoalType.TIME && dto.getTargetTimeSec() == null) {
            throw new IllegalArgumentException("Tempo alvo é obrigatório para objetivo do tipo TIME");
        }
        if (type == GoalType.PACE && dto.getPaceTargetSec() == null) {
            throw new IllegalArgumentException("Pace alvo é obrigatório para objetivo do tipo PACE");
        }
        if (type == GoalType.FREQUENCY && dto.getWeeklyFrequency() == null) {
            throw new IllegalArgumentException("Frequência semanal é obrigatória para objetivo do tipo FREQUENCY");
        }
        if (type == GoalType.RACE && dto.getRaceTargetId() == null) {
            throw new IllegalArgumentException("Prova alvo é obrigatória para objetivo do tipo RACE");
        }
    }
}
