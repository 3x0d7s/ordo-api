package com.kyut.ordo.board.service;

import com.kyut.ordo.board.entity.BoardEntity;
import com.kyut.ordo.board.entity.BoardRoleEntity;
import com.kyut.ordo.board.repository.BoardRoleRepository;
import com.kyut.ordo.common.role.RoleFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BoardRoleFactory {
    private final BoardRoleRepository boardRoleRepository;

    public BoardRoleEntity createOwnerRole(BoardEntity board) {
        return boardRoleRepository.save(BoardRoleEntity.builder()
                .name("Owner")
                .board(board)
                .ableToEdit(true)
                .ableToDelete(true)
                .ableToInviteMembers(true)
                .ableToManageRoles(true)
                .ableToCreateLists(true)
                .ableToCreateTasks(true)
                .build());
    }

    public BoardRoleEntity createMemberRole(BoardEntity board) {
        return boardRoleRepository.save(BoardRoleEntity.builder()
                .name("Member")
                .board(board)
                .ableToEdit(true)
                .ableToDelete(false)
                .ableToInviteMembers(false)
                .ableToManageRoles(false)
                .ableToCreateLists(true)
                .ableToCreateTasks(true)
                .build());
    }

    public BoardRoleEntity createGuestRole(BoardEntity board) {
        return boardRoleRepository.save(BoardRoleEntity.builder()
                .name("Guest")
                .board(board)
                .ableToEdit(false)
                .ableToDelete(false)
                .ableToInviteMembers(false)
                .ableToManageRoles(false)
                .ableToCreateLists(false)
                .ableToCreateTasks(true)
                .build());
    }
}
