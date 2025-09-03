package com.kyut.ordo.feature.workspace.mapper;

import com.kyut.ordo.feature.workspace.dto.WorkspaceCreate;
import com.kyut.ordo.feature.workspace.dto.WorkspaceRead;
import com.kyut.ordo.feature.workspace.dto.WorkspaceUpdate;
import com.kyut.ordo.feature.workspace.entity.WorkspaceEntity;

import com.kyut.ordo.feature.workspace.entity.WorkspaceRoleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.Collection;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WorkspaceMapper {
    WorkspaceEntity toEntity(WorkspaceCreate dto);

    WorkspaceRead toDto(WorkspaceEntity entity);

    @Mapping(source = "rolesCollection", target = "roles")
    WorkspaceRead toDto(WorkspaceEntity entity, Collection<WorkspaceRoleEntity> rolesCollection);

    void updateEntityFromDto(WorkspaceUpdate dto, @MappingTarget WorkspaceEntity taskList);
}
