package com.kyut.ordo.workspace.dto;

import lombok.Data;

@Data
public class WorkspaceMemberCreate {
    private Long userId;
    private Long workspaceId;
    private Long workspaceRoleId;
}
