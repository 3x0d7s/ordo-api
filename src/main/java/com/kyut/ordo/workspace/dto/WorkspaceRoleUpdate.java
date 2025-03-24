package com.kyut.ordo.workspace.dto;

import lombok.Data;

@Data
public class WorkspaceRoleUpdate {
    private String name;

    private boolean ableToManageMembers;
    private boolean ableToManageContent;
    private boolean ableToManageSettings;
    private boolean ableToManageRoles;
}
