package com.kyut.ordo.feature.workspace.dto;

import com.kyut.ordo.feature.user.dto.UserReadDTO;
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
