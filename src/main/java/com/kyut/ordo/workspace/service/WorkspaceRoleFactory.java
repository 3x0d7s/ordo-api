package com.kyut.ordo.workspace.service;

import com.kyut.ordo.common.role.RoleFactory;
import com.kyut.ordo.workspace.entity.WorkspaceEntity;
import com.kyut.ordo.workspace.entity.WorkspaceRoleEntity;
import com.kyut.ordo.workspace.repository.WorkspaceRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkspaceRoleFactory implements RoleFactory<WorkspaceRoleEntity, WorkspaceEntity> {
    private final WorkspaceRoleRepository workspaceRoleRepository;

    public WorkspaceRoleEntity createOwnerRole(WorkspaceEntity workspace) {
        return workspaceRoleRepository.save(WorkspaceRoleEntity.builder()
                .name("Owner")
                .workspace(workspace)
                .ableToManageMembers(true)
                .ableToManageContent(true)
                .ableToManageRoles(true)
                .ableToManageSettings(true)
                .build());
    }

    public WorkspaceRoleEntity createMemberRole(WorkspaceEntity workspace) {
        return workspaceRoleRepository.save(WorkspaceRoleEntity.builder()
                .name("Member")
                .workspace(workspace)
                .ableToManageMembers(false)
                .ableToManageContent(true)
                .ableToManageRoles(false)
                .ableToManageSettings(false)
                .build());
    }

    public WorkspaceRoleEntity createGuestRole(WorkspaceEntity workspace) {
        return workspaceRoleRepository.save(WorkspaceRoleEntity.builder()
                .name("Guest")
                .workspace(workspace)
                .ableToManageMembers(false)
                .ableToManageContent(false)
                .ableToManageRoles(false)
                .ableToManageSettings(false)
                .build());
    }


}
