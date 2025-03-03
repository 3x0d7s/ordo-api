package com.kyut.ordo.workspace.mapper;

import com.kyut.ordo.workspace.dto.WorkspaceCreate;
import com.kyut.ordo.workspace.dto.WorkspaceRead;
import com.kyut.ordo.workspace.entity.WorkspaceEntity;

import com.kyut.ordo.workspace.entity.WorkspaceRoleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Collection;

@Mapper(componentModel = "spring")
public interface WorkspaceMapper {
    WorkspaceEntity toEntity(WorkspaceCreate dto);

    WorkspaceRead toDto(WorkspaceEntity entity);

    @Mapping(source = "rolesCollection", target = "roles")
    WorkspaceRead toDto(WorkspaceEntity entity, Collection<WorkspaceRoleEntity> rolesCollection);
}
