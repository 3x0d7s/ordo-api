package com.kyut.ordo.feature.workspace.mapper;

import com.kyut.ordo.feature.workspace.dto.WorkspaceRoleCreate;
import com.kyut.ordo.feature.workspace.dto.WorkspaceRoleRead;
import com.kyut.ordo.feature.workspace.dto.WorkspaceRoleUpdate;
import com.kyut.ordo.feature.workspace.entity.WorkspaceRoleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WorkspaceRoleMapper {
    WorkspaceRoleRead toDto(WorkspaceRoleEntity entity);

    WorkspaceRoleEntity updateEntityFromDto(WorkspaceRoleUpdate dto, @MappingTarget WorkspaceRoleEntity role);

    WorkspaceRoleEntity toEntity(WorkspaceRoleCreate dto);
}
