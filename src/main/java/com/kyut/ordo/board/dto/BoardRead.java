package com.kyut.ordo.board.dto;

import com.kyut.ordo.board.entity.BoardVisibility;
import com.kyut.ordo.workspace.dto.WorkspaceRead;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class BoardRead {
    private Long id;
    private String title;
    private String description;
    private BoardVisibility visibility;
    private LocalDateTime createdAt;
    private List<BoardRoleRead> roles;
    private WorkspaceRead workspace;
}
