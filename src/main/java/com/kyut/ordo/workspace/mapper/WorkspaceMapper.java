package com.kyut.ordo.workspace.mapper;

import com.kyut.ordo.workspace.dto.WorkspaceCreate;
import com.kyut.ordo.workspace.dto.WorkspaceRead;
import com.kyut.ordo.workspace.entity.WorkspaceEntity;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WorkspaceMapper {
    WorkspaceEntity toEntity(WorkspaceCreate dto);

    WorkspaceRead toDto(WorkspaceEntity entity);
}
