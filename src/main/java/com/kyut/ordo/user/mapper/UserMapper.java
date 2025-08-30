package com.kyut.ordo.user.mapper;

import com.kyut.ordo.user.dto.UserCreateDTO;
import com.kyut.ordo.user.dto.UserReadDTO;

import com.kyut.ordo.user.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    UserEntity toEntity(UserCreateDTO dto);

    UserReadDTO toDto(UserEntity entity);
}
