package com.kyut.ordo.feature.workspace.dto;

import lombok.Data;

@Data
public class WorkspaceInviteCreate {
    private Long workspaceId;
    private Long roleId;
    private Integer expiresInDays;
} 