package com.kyut.ordo.feature.workspace.mapper;

import com.kyut.ordo.feature.workspace.dto.WorkspaceMemberRead;
import com.kyut.ordo.feature.workspace.entity.WorkspaceMemberEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WorkspaceMemberMapper {
    WorkspaceMemberRead toDto(WorkspaceMemberEntity entity);
}
