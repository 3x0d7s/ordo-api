package com.kyut.ordo.board.dto;

import com.kyut.ordo.user.dto.UserReadDTO;
import lombok.Data;

@Data
public class BoardMemberRead {
    private Long id;
    private UserReadDTO user;
    private BoardRead board;
    private BoardRoleRead role;
    private String joinedAt;
}
