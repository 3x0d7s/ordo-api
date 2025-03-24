package com.kyut.ordo.board.dto;

import lombok.Data;

@Data
public class BoardMemberRead {
    private Long id;
    private Long userId;
    private String username;
    private BoardRoleRead role;
    private String joinedAt;
}
