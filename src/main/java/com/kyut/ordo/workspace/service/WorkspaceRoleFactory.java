package com.kyut.ordo.workspace.service;

import com.kyut.ordo.common.role.RoleFactory;
import com.kyut.ordo.workspace.entity.WorkspaceRoleEntity;
import com.kyut.ordo.workspace.repository.WorkspaceRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkspaceRoleFactory implements RoleFactory<WorkspaceRoleEntity> {
    private final WorkspaceRoleRepository workspaceRoleRepository;

    @Override
    public WorkspaceRoleEntity createOwnerRole() {
        return workspaceRoleRepository.save(WorkspaceRoleEntity.builder()
                .name("Owner")
                .ableToManageMembers(true)
                .ableToManageContent(true)
                .ableToManageRoles(true)
                .ableToManageSettings(true)
                .build());
    }

    @Override
    public WorkspaceRoleEntity createMemberRole() {
        return workspaceRoleRepository.save(WorkspaceRoleEntity.builder()
                .name("Member")
                .ableToManageMembers(false)
                .ableToManageContent(true)
                .ableToManageRoles(false)
                .ableToManageSettings(false)
                .build());
    }

    @Override
    public WorkspaceRoleEntity createGuestRole() {
        return workspaceRoleRepository.save(WorkspaceRoleEntity.builder()
                .name("Guest")
                .ableToManageMembers(false)
                .ableToManageContent(false)
                .ableToManageRoles(false)
                .ableToManageSettings(false)
                .build());
    }
}
