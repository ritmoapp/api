package com.wellness.ritmo.domain.service;

import com.wellness.ritmo.domain.model.Enum.ConditioningLevel;
import com.wellness.ritmo.domain.model.User;
import com.wellness.ritmo.domain.model.UserProfile;
import com.wellness.ritmo.domain.repository.UserProfileRepository;
import com.wellness.ritmo.domain.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    private static final Map<ConditioningLevel, int[]> PACE_RANGES = Map.of(
            ConditioningLevel.BEGINNER, new int[]{420, 600},
            ConditioningLevel.INTERMEDIATE, new int[]{300, 480},
            ConditioningLevel.ADVANCED, new int[]{240, 360},
            ConditioningLevel.ATHLETE, new int[]{150, 300}
    );

    @Transactional
    public UserProfile createInitialProfile(
        Long userId,
        com.wellness.ritmo.domain.model.Enum.Gender gender,
        LocalDate birthDate,
        Integer heightCm,
        Double weightKg,
        ConditioningLevel conditioningLevel,
        Integer paceAvgSeg,
        Double weeklyMileageKm
    ) {
        log.info("[UserProfileService] Criando perfil inicial para usuário: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: " + userId));

        Optional<UserProfile> existingProfile = userProfileRepository.findByUserId(userId);
        if (existingProfile.isPresent()) {
            throw new IllegalStateException("Usuário já possui um perfil criado");
        }

        validatePaceForConditioningLevel(paceAvgSeg, conditioningLevel);

        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setGender(gender);
        profile.setBirthDate(birthDate);
        profile.setHeightCm(heightCm);
        profile.setWeightKg(weightKg);
        profile.setConditioningLevel(conditioningLevel);
        profile.setPaceAvgSeg(paceAvgSeg);
        profile.setWeeklyMileageKm(weeklyMileageKm);

        UserProfile savedProfile = userProfileRepository.save(profile);
        log.info("[UserProfileService] Perfil criado com sucesso para usuário: {}", userId);

        return savedProfile;
    }

    @Transactional
    public UserProfile updateProfile(
        Long userId,
        Double weightKg,
        ConditioningLevel conditioningLevel,
        Integer paceAvgSeg,
        Double weeklyMileageKm
    ) {
        log.info("[UserProfileService] Atualizando perfil do usuário: {}", userId);

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Perfil não encontrado para o usuário: " + userId));

        ConditioningLevel effectiveLevel = conditioningLevel != null
                ? conditioningLevel : profile.getConditioningLevel();
        Integer effectivePace = paceAvgSeg != null
                ? paceAvgSeg : profile.getPaceAvgSeg();

        if (effectivePace != null && effectiveLevel != null) {
            validatePaceForConditioningLevel(effectivePace, effectiveLevel);
        }

        if (weightKg != null) {
            profile.setWeightKg(weightKg);
        }
        if (conditioningLevel != null) {
            profile.setConditioningLevel(conditioningLevel);
        }
        if (paceAvgSeg != null) {
            profile.setPaceAvgSeg(paceAvgSeg);
        }
        if (weeklyMileageKm != null) {
            profile.setWeeklyMileageKm(weeklyMileageKm);
        }

        UserProfile updatedProfile = userProfileRepository.save(profile);
        log.info("[UserProfileService] Perfil atualizado com sucesso para usuário: {}", userId);

        return updatedProfile;
    }

    @Transactional(readOnly = true)
    public UserProfile getProfileByUserId(Long userId) {
        return userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Perfil não encontrado para o usuário: " + userId));
    }

    private void validatePaceForConditioningLevel(Integer paceAvgSeg, ConditioningLevel level) {
        if (paceAvgSeg == null || level == null) {
            return;
        }

        int[] range = PACE_RANGES.get(level);
        if (paceAvgSeg < range[0] || paceAvgSeg > range[1]) {
            String minFormatted = formatPace(range[0]);
            String maxFormatted = formatPace(range[1]);
            throw new IllegalArgumentException(
                    String.format("Pace %s incompatível com nível %s. Faixa esperada: %s a %s por km",
                            formatPace(paceAvgSeg), level, minFormatted, maxFormatted));
        }
    }

    private String formatPace(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}
