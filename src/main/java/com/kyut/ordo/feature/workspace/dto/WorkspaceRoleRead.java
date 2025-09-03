package com.kyut.ordo.feature.workspace.dto;

import lombok.Data;

@Data
public class WorkspaceRoleRead {
    private Long id;
    private String name;

    private boolean ableToManageMembers;
    private boolean ableToManageContent;
    private boolean ableToManageSettings;
    private boolean ableToManageRoles;
}
