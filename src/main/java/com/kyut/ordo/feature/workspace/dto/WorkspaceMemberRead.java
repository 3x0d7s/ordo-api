package com.kyut.ordo.feature.workspace.dto;

import com.kyut.ordo.feature.user.dto.UserReadDTO;
import lombok.Data;

@Data
public class WorkspaceMemberRead {
    private Long id;
    private UserReadDTO user;
    private WorkspaceRead workspace;
    private WorkspaceRoleRead role;
}
