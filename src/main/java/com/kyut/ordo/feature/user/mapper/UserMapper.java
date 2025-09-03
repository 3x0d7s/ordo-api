package com.kyut.ordo.feature.user.mapper;

import com.kyut.ordo.feature.user.dto.UserCreateDTO;
import com.kyut.ordo.feature.user.dto.UserReadDTO;

import com.kyut.ordo.feature.user.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    UserEntity toEntity(UserCreateDTO dto);

    UserReadDTO toDto(UserEntity entity);
}
