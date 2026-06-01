package com.wellness.ritmo.domain.service;

import com.wellness.ritmo.api.dto.AvailabilityRequestDto;
import com.wellness.ritmo.domain.model.User;
import com.wellness.ritmo.domain.model.UserAvailability;
import com.wellness.ritmo.domain.repository.UserAvailabilityRepository;
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
public class AvailabilityService {

    private final UserAvailabilityRepository availabilityRepository;
    private final UserRepository userRepository;

    @Transactional
    public UserAvailability create(Long userId, AvailabilityRequestDto dto) {
        log.info("[AvailabilityService] Criando disponibilidade para usuário: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: " + userId));

        validateTimeRange(dto);

        UserAvailability availability = new UserAvailability();
        availability.setUser(user);
        availability.setDayOfWeek(dto.getDayOfWeek());
        availability.setStartTime(dto.getStartTime());
        availability.setEndTime(dto.getEndTime());
        availability.setMaxSessionMinutes(dto.getMaxSessionMinutes());
        availability.setPreferredIntensity(dto.getPreferredIntensity());
        availability.setValidFrom(dto.getValidFrom());
        availability.setValidUntil(dto.getValidUntil());

        UserAvailability saved = availabilityRepository.save(availability);
        log.info("[AvailabilityService] Disponibilidade criada com sucesso: {}", saved.getId());
        return saved;
    }

    @Transactional
    public List<UserAvailability> createBatch(Long userId, List<AvailabilityRequestDto> dtos) {
        log.info("[AvailabilityService] Criando {} disponibilidades para usuário: {}", dtos.size(), userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: " + userId));

        return dtos.stream().map(dto -> {
            validateTimeRange(dto);

            UserAvailability availability = new UserAvailability();
            availability.setUser(user);
            availability.setDayOfWeek(dto.getDayOfWeek());
            availability.setStartTime(dto.getStartTime());
            availability.setEndTime(dto.getEndTime());
            availability.setMaxSessionMinutes(dto.getMaxSessionMinutes());
            availability.setPreferredIntensity(dto.getPreferredIntensity());
            availability.setValidFrom(dto.getValidFrom());
            availability.setValidUntil(dto.getValidUntil());

            return availabilityRepository.save(availability);
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<UserAvailability> getValidByUser(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: " + userId));
        return availabilityRepository.findValidByUserId(userId);
    }

    @Transactional
    public void delete(Long userId, Long availabilityId) {
        UserAvailability availability = availabilityRepository.findById(availabilityId)
                .filter(a -> a.getUser().getId().equals(userId))
                .orElseThrow(() -> new EntityNotFoundException(
                        "Disponibilidade não encontrada para o usuário: " + userId));
        availabilityRepository.delete(availability);
        log.info("[AvailabilityService] Disponibilidade {} removida para usuário: {}", availabilityId, userId);
    }

    private void validateTimeRange(AvailabilityRequestDto dto) {
        if (dto.getEndTime().isBefore(dto.getStartTime()) || dto.getEndTime().equals(dto.getStartTime())) {
            throw new IllegalArgumentException("Horário de término deve ser posterior ao horário de início");
        }
        if (dto.getValidUntil() != null && dto.getValidUntil().isBefore(dto.getValidFrom())) {
            throw new IllegalArgumentException("Data final de validade deve ser posterior à data inicial");
        }
    }
}
