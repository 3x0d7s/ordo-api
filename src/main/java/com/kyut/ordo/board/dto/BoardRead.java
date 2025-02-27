package com.kyut.ordo.board.dto;

import com.kyut.ordo.board.entity.BoardVisibility;
import com.kyut.ordo.workspace.dto.WorkspaceRead;
import lombok.Data;

@Data
public class BoardRead {
    private Long id;
    private String title;
    private String description;
    private BoardVisibility visibility;
    private WorkspaceRead workspace;
}
