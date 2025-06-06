package com.kyut.ordo.workspace.mapper;

import com.kyut.ordo.workspace.dto.WorkspaceRoleCreate;
import com.kyut.ordo.workspace.dto.WorkspaceRoleRead;
import com.kyut.ordo.workspace.dto.WorkspaceRoleUpdate;
import com.kyut.ordo.workspace.entity.WorkspaceRoleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface WorkspaceRoleMapper {
    WorkspaceRoleRead toDto(WorkspaceRoleEntity entity);

    WorkspaceRoleEntity updateEntityFromDto(WorkspaceRoleUpdate dto, @MappingTarget WorkspaceRoleEntity role);

    WorkspaceRoleEntity toEntity(WorkspaceRoleCreate dto);
}
