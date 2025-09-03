package com.kyut.ordo.feature.workspace.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WorkspaceInviteRead {
    private Long id;
    private String token;
    private String inviteUrl;
    private WorkspaceRead workspace;
    private WorkspaceRoleRead role;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
} 