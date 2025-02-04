package com.kyut.ordo.user;

import com.kyut.ordo.user.dto.UserCreateDTO;
import com.kyut.ordo.user.dto.UserReadDTO;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserEntity toEntity(UserCreateDTO dto);

    UserReadDTO toDto(UserEntity entity);
}
