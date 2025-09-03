package com.kyut.ordo.feature.board.dto;

import com.kyut.ordo.feature.board.entity.BoardVisibility;
import lombok.Data;

@Data
public class BoardCreate {
    private String title;
    private String description;
    private String color;
    private BoardVisibility visibility;
    private Long workspaceId;
}
