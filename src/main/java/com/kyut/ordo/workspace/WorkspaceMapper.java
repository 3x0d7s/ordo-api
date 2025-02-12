package com.kyut.ordo.workspace;

import com.kyut.ordo.workspace.dto.WorkspaceCreate;
import com.kyut.ordo.workspace.dto.WorkspaceRead;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WorkspaceMapper {
    WorkspaceEntity toEntity(WorkspaceCreate dto);

    WorkspaceRead toDto(WorkspaceEntity entity);
}
