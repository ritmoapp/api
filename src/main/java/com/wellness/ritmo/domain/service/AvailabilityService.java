package com.wellness.ritmo.domain.service;

import com.wellness.ritmo.domain.model.Enum.IntensityPreference;
import com.wellness.ritmo.domain.model.User;
import com.wellness.ritmo.domain.model.UserAvailability;
import com.wellness.ritmo.domain.repository.UserAvailabilityRepository;
import com.wellness.ritmo.domain.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class AvailabilityService {

    private final UserAvailabilityRepository availabilityRepository;
    private final UserRepository userRepository;

    @Transactional
    public UserAvailability create(Long userId, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime,
                                   Integer maxSessionMinutes, IntensityPreference preferredIntensity,
                                   LocalDate validFrom, LocalDate validUntil) {
        log.info("[AvailabilityService] Criando disponibilidade para usuário: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: " + userId));

        validateTimeRange(startTime, endTime, validFrom, validUntil);

        UserAvailability availability = new UserAvailability();
        availability.setUser(user);
        availability.setDayOfWeek(dayOfWeek);
        availability.setStartTime(startTime);
        availability.setEndTime(endTime);
        availability.setMaxSessionMinutes(maxSessionMinutes);
        availability.setPreferredIntensity(preferredIntensity);
        availability.setValidFrom(validFrom);
        availability.setValidUntil(validUntil);

        UserAvailability saved = availabilityRepository.save(availability);
        log.info("[AvailabilityService] Disponibilidade criada com sucesso: {}", saved.getId());
        return saved;
    }

    @Transactional
    public List<UserAvailability> createBatch(Long userId, List<AvailabilityData> availabilityDataList) {
        log.info("[AvailabilityService] Criando {} disponibilidades para usuário: {}", availabilityDataList.size(), userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: " + userId));

        return availabilityDataList.stream().map(data -> {
            validateTimeRange(data.startTime, data.endTime, data.validFrom, data.validUntil);

            UserAvailability availability = new UserAvailability();
            availability.setUser(user);
            availability.setDayOfWeek(data.dayOfWeek);
            availability.setStartTime(data.startTime);
            availability.setEndTime(data.endTime);
            availability.setMaxSessionMinutes(data.maxSessionMinutes);
            availability.setPreferredIntensity(data.preferredIntensity);
            availability.setValidFrom(data.validFrom);
            availability.setValidUntil(data.validUntil);

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

    private void validateTimeRange(LocalTime startTime, LocalTime endTime, LocalDate validFrom, LocalDate validUntil) {
        if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
            throw new IllegalArgumentException("Horário de término deve ser posterior ao horário de início");
        }
        if (validUntil != null && validUntil.isBefore(validFrom)) {
            throw new IllegalArgumentException("Data final de validade deve ser posterior à data inicial");
        }
    }

    public static class AvailabilityData {
        public final DayOfWeek dayOfWeek;
        public final LocalTime startTime;
        public final LocalTime endTime;
        public final Integer maxSessionMinutes;
        public final IntensityPreference preferredIntensity;
        public final LocalDate validFrom;
        public final LocalDate validUntil;

        public AvailabilityData(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime,
                               Integer maxSessionMinutes, IntensityPreference preferredIntensity,
                               LocalDate validFrom, LocalDate validUntil) {
            this.dayOfWeek = dayOfWeek;
            this.startTime = startTime;
            this.endTime = endTime;
            this.maxSessionMinutes = maxSessionMinutes;
            this.preferredIntensity = preferredIntensity;
            this.validFrom = validFrom;
            this.validUntil = validUntil;
        }
    }
}
