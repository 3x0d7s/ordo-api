package com.kyut.ordo.feature.workspace.dto;

import lombok.Data;

@Data
public class WorkspaceInviteCreate {
    private final Long workspaceId;
    private final Long roleId;
    private final Integer expiresInDays;
} 