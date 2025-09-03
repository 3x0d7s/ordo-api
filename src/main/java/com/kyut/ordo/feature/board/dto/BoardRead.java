package com.kyut.ordo.feature.board.dto;

import com.kyut.ordo.feature.board.entity.BoardVisibility;
import com.kyut.ordo.feature.workspace.dto.WorkspaceRead;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class BoardRead {
    private Long id;
    private String title;
    private String description;
    private String color;
    private BoardVisibility visibility;
    private LocalDateTime createdAt;
    private List<BoardRoleRead> roles;
    private WorkspaceRead workspace;
}
