package com.kyut.ordo.feature.workspace.dto;

import lombok.Data;

@Data
public class WorkspaceRoleCreate {
    private String name;
    private Long workspaceId;

    private boolean ableToManageMembers;
    private boolean ableToManageContent;
    private boolean ableToManageSettings;
    private boolean ableToManageRoles;
}
