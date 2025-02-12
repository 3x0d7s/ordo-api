package com.kyut.ordo.workspace.dto;

import com.kyut.ordo.user.dto.UserReadDTO;
import lombok.Data;

@Data
public class WorkspaceRead {
    private Long id;
    private String title;
    private String description;
    private UserReadDTO owner;
}
