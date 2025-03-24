package com.kyut.ordo.workspace.mapper;

import com.kyut.ordo.workspace.dto.WorkspaceMemberRead;
import com.kyut.ordo.workspace.entity.WorkspaceMemberEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WorkspaceMemberMapper {
    WorkspaceMemberRead toDto(WorkspaceMemberEntity entity);
}
