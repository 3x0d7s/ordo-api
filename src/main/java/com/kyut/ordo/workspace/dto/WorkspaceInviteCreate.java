package com.kyut.ordo.workspace.dto;

import lombok.Data;

@Data
public class WorkspaceInviteCreate {
    private final Long workspaceId;
    private final Long roleId;
    private final Integer expiresInDays;
} 