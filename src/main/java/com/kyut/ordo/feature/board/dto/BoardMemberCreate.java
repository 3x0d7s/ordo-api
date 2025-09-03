package com.kyut.ordo.feature.board.dto;

import lombok.Data;

@Data
public class BoardMemberCreate {
    private Long userId;
    private Long boardRoleId;
}
