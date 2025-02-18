package com.kyut.ordo.workspace.dto;

import com.kyut.ordo.user.dto.UserReadDTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class WorkspaceRead {
    private Long id;
    private String title;
    private String description;
    private UserReadDTO owner;
    private List<WorkspaceRoleRead> roles;
    private LocalDateTime createdAt;
}
