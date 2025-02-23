package com.kyut.ordo.board.dto;

import com.kyut.ordo.board.entity.BoardVisibility;
import lombok.Data;

@Data
public class BoardCreate {
    private String title;
    private String description;
    private BoardVisibility visibility;
    private Long workspaceId;
}
