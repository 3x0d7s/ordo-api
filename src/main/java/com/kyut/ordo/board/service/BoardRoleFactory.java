package com.kyut.ordo.board.service;

import com.kyut.ordo.board.entity.BoardRoleEntity;
import com.kyut.ordo.board.repository.BoardRoleRepository;
import com.kyut.ordo.common.role.RoleFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BoardRoleFactory implements RoleFactory<BoardRoleEntity> {
    private final BoardRoleRepository boardRoleRepository;

    @Override
    public BoardRoleEntity createOwnerRole() {
        return boardRoleRepository.save(BoardRoleEntity.builder()
                .name("Owner")
                .ableToEdit(true)
                .ableToDelete(true)
                .ableToInviteMembers(true)
                .ableToManageRoles(true)
                .ableToCreateLists(true)
                .ableToCreateTasks(true)
                .build());
    }

    @Override
    public BoardRoleEntity createMemberRole() {
        return boardRoleRepository.save(BoardRoleEntity.builder()
                .name("Member")
                .ableToEdit(true)
                .ableToDelete(false)
                .ableToInviteMembers(false)
                .ableToManageRoles(false)
                .ableToCreateLists(true)
                .ableToCreateTasks(true)
                .build());
    }

    @Override
    public BoardRoleEntity createGuestRole() {
        return boardRoleRepository.save(BoardRoleEntity.builder()
                .name("Guest")
                .ableToEdit(false)
                .ableToDelete(false)
                .ableToInviteMembers(false)
                .ableToManageRoles(false)
                .ableToCreateLists(false)
                .ableToCreateTasks(true)
                .build());
    }
}
