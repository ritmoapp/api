package com.wellness.ritmo.api.dto.mapper;

import com.wellness.ritmo.api.dto.UserProfileResponseDto;
import com.wellness.ritmo.domain.model.UserProfile;

public class UserProfileMapper {

    private UserProfileMapper() {
    }

    public static UserProfileResponseDto toDto(UserProfile profile) {
        UserProfileResponseDto dto = new UserProfileResponseDto();
        dto.setId(profile.getId());
        dto.setUserId(profile.getUser().getId());
        dto.setGender(profile.getGender());
        dto.setBirthDate(profile.getBirthDate());
        dto.setHeightCm(profile.getHeightCm());
        dto.setWeightKg(profile.getWeightKg());
        dto.setConditioningLevel(profile.getConditioningLevel());
        dto.setPaceAvgSeg(profile.getPaceAvgSeg());
        dto.setWeeklyMileageKm(profile.getWeeklyMileageKm());
        dto.setCreatedAt(profile.getCreatedAt());
        dto.setUpdatedAt(profile.getUpdatedAt());
        return dto;
    }
}
