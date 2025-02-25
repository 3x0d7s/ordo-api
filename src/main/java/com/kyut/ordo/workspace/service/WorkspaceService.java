package com.kyut.ordo.workspace.service;

import com.kyut.ordo.user.UserEntity;
import com.kyut.ordo.workspace.dto.WorkspaceCreate;
import com.kyut.ordo.workspace.dto.WorkspaceRead;

import com.kyut.ordo.workspace.dto.WorkspaceRoleRead;
import com.kyut.ordo.workspace.entity.WorkspaceEntity;
import com.kyut.ordo.workspace.entity.WorkspaceRoleEntity;
import com.kyut.ordo.workspace.entity.WorkspaceMemberEntity;
import com.kyut.ordo.workspace.exception.WorkspaceNotFoundException;
import com.kyut.ordo.workspace.exception.WorkspaceRoleInsuficientRightsExceptions;
import com.kyut.ordo.workspace.mapper.WorkspaceMapper;
import com.kyut.ordo.workspace.mapper.WorkspaceRoleMapper;
import com.kyut.ordo.workspace.repository.WorkspaceMemberRepository;
import com.kyut.ordo.workspace.repository.WorkspaceRepository;
import com.kyut.ordo.workspace.repository.WorkspaceRoleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkspaceService {
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceRoleRepository workspaceRoleRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    private final WorkspaceRoleMapper workspaceRoleMapper;
    private final WorkspaceMapper workspaceMapper;

    public Page<WorkspaceRead> findAllByOwner(UserEntity user, Pageable pageable) {
        return workspaceRepository
                .findAllByOwner(user, pageable)
                .map(workspaceMapper::toDto);
    }

    public WorkspaceRead findById(UserEntity user, long id) throws WorkspaceNotFoundException {
        WorkspaceEntity result = workspaceRepository
                .findById(id)
                .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found by this id"));
        return workspaceMapper.toDto(result);
    }

    @Transactional
    public WorkspaceRead createWorkspace(UserEntity user, WorkspaceCreate dto) {
        WorkspaceEntity workspace = workspaceMapper.toEntity(dto);
        workspace.setOwner(user);
        workspaceRepository.save(workspace);

        WorkspaceRoleEntity ownerRole = workspaceRoleRepository.save(WorkspaceRoleEntity.builder()
                .workspace(workspace)
                .name("Owner")
                .ableToManageMembers(true)
                .ableToManageContent(true)
                .ableToManageRoles(true)
                .ableToManageSettings(true)
                .build());

        workspaceRoleRepository.save(WorkspaceRoleEntity.builder()
                .workspace(workspace)
                .name("Member")
                .ableToManageMembers(false)
                .ableToManageContent(true)
                .ableToManageRoles(false)
                .ableToManageSettings(false)
                .build());

        workspaceRoleRepository.save(WorkspaceRoleEntity.builder()
                .workspace(workspace)
                .name("Guest")
                .ableToManageMembers(false)
                .ableToManageContent(false)
                .ableToManageRoles(false)
                .ableToManageSettings(false)
                .build());

        WorkspaceMemberEntity workspaceMember = WorkspaceMemberEntity.builder()
                .workspace(workspace)
                .user(user)
                .role(ownerRole)
                .build();

        workspaceMemberRepository.save(workspaceMember);

        return workspaceMapper.toDto(workspace);
    }

    public Page<WorkspaceRoleRead> findRolesByWorkspaceId(Long id, Pageable pageable) throws WorkspaceNotFoundException {
        WorkspaceEntity workspace = workspaceRepository
                .findById(id)
                .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found by this id"));

        return workspaceRoleRepository
                .findAllByWorkspace(workspace, pageable)
                .map(workspaceRoleMapper::toDto);
    }

    public WorkspaceRead deleteWorkspace(UserEntity user, Long id)
            throws WorkspaceNotFoundException, WorkspaceRoleInsuficientRightsExceptions {
        WorkspaceEntity workspace = workspaceRepository
                .findById(id)
                .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found by this id"));

        WorkspaceMemberEntity workspaceMember = workspaceMemberRepository.findByWorkspaceAndUser(workspace, user);
        WorkspaceRoleEntity role = workspaceMember.getRole();

        if (!role.isAbleToManageSettings()) {
            throw new WorkspaceRoleInsuficientRightsExceptions("You don't have permission to delete this workspace");
        }

        workspaceRepository.delete(workspace);

        return workspaceMapper.toDto(workspace);
    }

}
