package com.kyut.ordo.workspace.mapper;

import com.kyut.ordo.workspace.dto.WorkspaceRoleRead;
import com.kyut.ordo.workspace.entity.WorkspaceRoleEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WorkspaceRoleMapper {
    WorkspaceRoleRead toDto(WorkspaceRoleEntity entity);
}
