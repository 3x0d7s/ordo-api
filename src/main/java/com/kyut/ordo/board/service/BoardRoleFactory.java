package com.kyut.ordo.board.service;

import com.kyut.ordo.board.entity.BoardEntity;
import com.kyut.ordo.board.entity.BoardRoleEntity;
import com.kyut.ordo.board.repository.BoardRoleRepository;
import com.kyut.ordo.common.role.RoleFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class BoardRoleFactory implements RoleFactory<BoardRoleEntity, BoardEntity> {
    private final BoardRoleRepository boardRoleRepository;

    @Override
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

    @Override
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

    @Override
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
