package com.kyut.ordo.feature.workspace.mapper;

import com.kyut.ordo.feature.workspace.dto.WorkspaceInviteRead;
import com.kyut.ordo.feature.workspace.entity.WorkspaceInviteEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {WorkspaceMapper.class, WorkspaceRoleMapper.class})
public interface WorkspaceInviteMapper {
    @Mapping(target = "inviteUrl", expression = "java(\"/workspaces/join/\" + entity.getToken())")
    WorkspaceInviteRead toDto(WorkspaceInviteEntity entity);
} 