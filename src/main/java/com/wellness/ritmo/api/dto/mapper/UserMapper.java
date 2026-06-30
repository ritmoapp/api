package com.wellness.ritmo.api.dto.mapper;

import com.wellness.ritmo.api.dto.UserCreateDto;
import com.wellness.ritmo.api.dto.UserResponseDto;
import com.wellness.ritmo.domain.model.User;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

import java.util.List;
import java.util.stream.Collectors;

public class UserMapper {

    public static User toUser(UserCreateDto createDto){
        return new ModelMapper().map(createDto, User.class);
    }

    public static UserResponseDto toDto(User user){
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setCreatedOn(user.getCreatedOn());
        if (user.getProfile() != null) {
            dto.setUserProfileResponseDto(UserProfileMapper.toDto(user.getProfile()));
        }
        return dto;
    }

    public static List<UserResponseDto> toListDto(List<User> users){
        return users.stream().map(UserMapper::toDto).collect(Collectors.toList());
    }
}
