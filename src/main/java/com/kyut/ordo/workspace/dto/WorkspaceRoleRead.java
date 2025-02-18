package com.kyut.ordo.workspace.dto;

import lombok.Data;

@Data
public class WorkspaceRoleRead {
    private Long id;
    private String name;

    private boolean canManageMembers;
    private boolean canManageContent;
    private boolean canManageSettings;
    private boolean canManageRoles;
}
