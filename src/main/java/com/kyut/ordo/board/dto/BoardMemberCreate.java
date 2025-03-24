package com.kyut.ordo.board.dto;

import lombok.Data;

@Data
public class BoardMemberCreate {
    private Long userId;
    private Long boardRoleId;
}
