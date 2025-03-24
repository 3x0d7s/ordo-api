package com.kyut.ordo.workspace.dto;

import com.kyut.ordo.user.dto.UserReadDTO;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WorkspaceMemberRead {
    private Long id;
    private UserReadDTO user;
    private WorkspaceRead workspace;
    private WorkspaceRoleRead role;
}
