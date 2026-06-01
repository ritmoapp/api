package com.wellness.ritmo.api.dto.mapper;

import com.wellness.ritmo.api.dto.AvailabilityResponseDto;
import com.wellness.ritmo.domain.model.UserAvailability;

public class AvailabilityMapper {

    private AvailabilityMapper() {
    }

    public static AvailabilityResponseDto toDto(UserAvailability availability) {
        AvailabilityResponseDto dto = new AvailabilityResponseDto();
        dto.setId(availability.getId());
        dto.setUserId(availability.getUser().getId());
        dto.setDayOfWeek(availability.getDayOfWeek());
        dto.setStartTime(availability.getStartTime());
        dto.setEndTime(availability.getEndTime());
        dto.setMaxSessionMinutes(availability.getMaxSessionMinutes());
        dto.setPreferredIntensity(availability.getPreferredIntensity());
        dto.setValidFrom(availability.getValidFrom());
        dto.setValidUntil(availability.getValidUntil());
        return dto;
    }
}
