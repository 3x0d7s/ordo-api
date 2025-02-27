package com.kyut.ordo.board.dto;

import lombok.Data;

@Data
public class BoardRoleRead {
    private boolean ableToEdit;
    private boolean ableToDelete;
    private boolean ableToInviteMembers;
    private boolean ableToManageRoles;
    private boolean ableToCreateLists;
    private boolean ableToCreateTasks;
}
