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
        PropertyMap<User,UserResponseDto> props = new PropertyMap<User, UserResponseDto>() {
            @Override
            protected void configure() {

            }
        };
        ModelMapper mapper = new ModelMapper();
        mapper.addMappings(props);
        return mapper.map(user, UserResponseDto.class);
    }

    public static List<UserResponseDto> toListDto(List<User> users){
        return users.stream().map(UserMapper::toDto).collect(Collectors.toList());
    }
}
