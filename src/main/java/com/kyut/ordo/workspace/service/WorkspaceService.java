package com.kyut.ordo.workspace.service;

import com.kyut.ordo.user.UserEntity;
import com.kyut.ordo.workspace.dto.WorkspaceCreate;
import com.kyut.ordo.workspace.dto.WorkspaceRead;

import com.kyut.ordo.workspace.dto.WorkspaceRoleRead;
import com.kyut.ordo.workspace.entity.WorkspaceEntity;
import com.kyut.ordo.workspace.entity.WorkspaceRoleEntity;
import com.kyut.ordo.workspace.exception.WorkspaceNotFoundException;
import com.kyut.ordo.workspace.mapper.WorkspaceMapper;
import com.kyut.ordo.workspace.mapper.WorkspaceRoleMapper;
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

    public WorkspaceRead createWorkspace(UserEntity user, WorkspaceCreate dto) {
        WorkspaceEntity workspace = workspaceMapper.toEntity(dto);
        workspace.setOwner(user);
        workspaceRepository.save(workspace);

        workspaceRoleRepository.save(WorkspaceRoleEntity.builder()
                .workspace(workspace)
                .name("Owner")
                .canManageMembers(true)
                .canManageContent(true)
                .canManageRoles(true)
                .canManageSettings(true)
                .build());

        workspaceRoleRepository.save(WorkspaceRoleEntity.builder()
                .workspace(workspace)
                .name("Member")
                .canManageMembers(false)
                .canManageContent(true)
                .canManageRoles(false)
                .canManageSettings(false)
                .build());

        workspaceRoleRepository.save(WorkspaceRoleEntity.builder()
                .workspace(workspace)
                .name("Guest")
                .canManageMembers(false)
                .canManageContent(false)
                .canManageRoles(false)
                .canManageSettings(false)
                .build());



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

    @Transactional
    public WorkspaceRead deleteWorkspace(UserEntity user, Long id) throws WorkspaceNotFoundException {
        WorkspaceEntity workspace = workspaceRepository
                .findById(id)
                .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found by this id"));

        workspaceRoleRepository.deleteAllByWorkspace(workspace);
        workspaceRepository.delete(workspace);
        return workspaceMapper.toDto(workspace);
    }

}
