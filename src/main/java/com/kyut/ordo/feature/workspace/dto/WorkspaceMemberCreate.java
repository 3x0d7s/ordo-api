package com.kyut.ordo.feature.workspace.dto;

import lombok.Data;

@Data
public class WorkspaceMemberCreate {
    private Long userId;
    private Long workspaceId;
    private Long workspaceRoleId;
}
