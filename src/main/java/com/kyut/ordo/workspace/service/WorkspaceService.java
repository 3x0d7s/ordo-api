package com.kyut.ordo.workspace.service;

import com.kyut.ordo.user.entity.UserEntity;
import com.kyut.ordo.workspace.dto.*;

import com.kyut.ordo.workspace.entity.WorkspaceEntity;
import com.kyut.ordo.workspace.entity.WorkspaceRoleEntity;
import com.kyut.ordo.workspace.entity.WorkspaceMemberEntity;
import com.kyut.ordo.workspace.exception.WorkspaceNotFoundException;
import com.kyut.ordo.workspace.exception.WorkspaceRoleInsuficientRightsExceptions;
import com.kyut.ordo.workspace.mapper.WorkspaceMapper;
import com.kyut.ordo.workspace.mapper.WorkspaceMemberMapper;
import com.kyut.ordo.workspace.mapper.WorkspaceRoleMapper;
import com.kyut.ordo.workspace.mapper.WorkspaceRoleMapperImpl;
import com.kyut.ordo.workspace.repository.WorkspaceMemberRepository;
import com.kyut.ordo.workspace.repository.WorkspaceRepository;
import com.kyut.ordo.workspace.repository.WorkspaceRoleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class WorkspaceService {
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceRoleRepository workspaceRoleRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final WorkspaceRoleFactory workspaceRoleFactory;

    private final WorkspaceRoleMapper workspaceRoleMapper;
    private final WorkspaceMemberMapper workspaceMemberMapper;
    private final WorkspaceMapper workspaceMapper;
    private final WorkspaceRoleMapperImpl workspaceRoleMapperImpl;

    public Page<WorkspaceRead> findAllByOwner(UserEntity user, Pageable pageable) {
        return workspaceRepository
                .findAllByOwner(user, pageable)
                .map(workspaceMapper::toDto);
    }

    public Page<WorkspaceRead> findAllByMember(UserEntity user, Pageable pageable) {
        return workspaceRepository
                .findAllByMembersUser(user, pageable)
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

        Map<String, WorkspaceRoleEntity> rolesMap = workspaceRoleFactory.rolesAsMap(workspace);

        WorkspaceMemberEntity workspaceMember = WorkspaceMemberEntity.builder()
                .workspace(workspace)
                .user(user)
                .role(rolesMap.get("Owner"))
                .build();

        workspaceMemberRepository.save(workspaceMember);

        return workspaceMapper.toDto(workspace, rolesMap.values());
    }

    public Page<WorkspaceRoleRead> findRolesByWorkspaceId(Long id, Pageable pageable) throws WorkspaceNotFoundException {
        WorkspaceEntity workspace = workspaceRepository
                .findById(id)
                .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found by this id"));

        return workspaceRoleRepository
                .findAllByWorkspace(workspace, pageable)
                .map(workspaceRoleMapper::toDto);
    }

    public Page<WorkspaceMemberRead> findMembersByWorkspaceId(Long id, Pageable pageable) throws WorkspaceNotFoundException {
        WorkspaceEntity workspace = workspaceRepository
                .findById(id)
                .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found by this id"));

        return workspaceMemberRepository
                .findAllByWorkspace(workspace, pageable)
                .map(workspaceMemberMapper::toDto);
    }

    public WorkspaceRead deleteWorkspace(UserEntity user, Long id)
            throws WorkspaceNotFoundException, WorkspaceRoleInsuficientRightsExceptions {
        WorkspaceEntity workspace = workspaceRepository
                .findById(id)
                .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found by this id"));

        WorkspaceMemberEntity workspaceMember = workspaceMemberRepository.findByWorkspaceAndUser(workspace, user)
                .orElseThrow(() -> new WorkspaceNotFoundException("WorkspaceMember not found by this id"));
        WorkspaceRoleEntity role = workspaceMember.getRole();

        if (!role.isAbleToManageSettings()) {
            throw new WorkspaceRoleInsuficientRightsExceptions("You don't have permission to delete this workspace");
        }

        workspaceRepository.delete(workspace);

        return workspaceMapper.toDto(workspace);
    }

    public WorkspaceRead updateWorkspace(
            UserEntity user,
            Long id,
            WorkspaceUpdate dto) throws WorkspaceNotFoundException, WorkspaceRoleInsuficientRightsExceptions {
        WorkspaceEntity workspace = workspaceRepository
                .findById(id)
                .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found by this id"));

        WorkspaceMemberEntity workspaceMember = workspaceMemberRepository.findByWorkspaceAndUser(workspace, user)
                .orElseThrow(() -> new WorkspaceNotFoundException("WorkspaceMember not found by this id"));
        WorkspaceRoleEntity role = workspaceMember.getRole();

        if (!role.isAbleToManageSettings()) {
            throw new WorkspaceRoleInsuficientRightsExceptions("You don't have permission to update this workspace");
        }

        workspaceMapper.updateEntityFromDto(dto, workspace);

        return workspaceMapper.toDto(workspace);
    }

    public WorkspaceMemberRead updateMember(UserEntity user,
                                      Long workspaceId,
                                      Long userId,
                                      WorkspaceMemberUpdate dto) throws WorkspaceNotFoundException, WorkspaceRoleInsuficientRightsExceptions {

        WorkspaceEntity workspace = workspaceRepository
                .findById(workspaceId)
                .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found by this id"));

        WorkspaceMemberEntity workspaceMember = workspaceMemberRepository
                .findByWorkspaceAndUser(workspace, user)
                .orElseThrow(() -> new WorkspaceNotFoundException("WorkspaceMember not found by this id"));

        if (!workspaceMember.getRole().isAbleToManageSettings()) {
            throw new WorkspaceRoleInsuficientRightsExceptions("You don't have permission to update roles in this workspace");
        }

        WorkspaceMemberEntity member = workspaceMemberRepository
                .findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new WorkspaceNotFoundException("WorkspaceMember not found by this id"));

        WorkspaceRoleEntity role = workspaceRoleRepository
                .findById(dto.getWorkspaceRoleId())
                .orElseThrow(() -> new WorkspaceNotFoundException("WorkspaceRole not found by this id"));

        member.setRole(role);

        return workspaceMemberMapper.toDto(member);
    }

    public WorkspaceMemberRead deleteMember(UserEntity user,
                                      Long workspaceId,
                                      Long userId) throws WorkspaceNotFoundException, WorkspaceRoleInsuficientRightsExceptions {

        WorkspaceEntity workspace = workspaceRepository
                .findById(workspaceId)
                .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found by this id"));

        WorkspaceMemberEntity workspaceMember = workspaceMemberRepository
                .findByWorkspaceAndUser(workspace, user)
                .orElseThrow(() -> new WorkspaceNotFoundException("WorkspaceMember not found by this id"));

        if (!workspaceMember.getRole().isAbleToManageSettings()) {
            throw new WorkspaceRoleInsuficientRightsExceptions("You don't have permission to delete members in this workspace");
        }

        WorkspaceMemberEntity member = workspaceMemberRepository
                .findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new WorkspaceNotFoundException("WorkspaceMember not found by this id"));

        workspaceMemberRepository.delete(member);

        return workspaceMemberMapper.toDto(member);
    }

    public WorkspaceRoleRead getMyRole(UserEntity user,
                                       Long workspaceId) throws WorkspaceNotFoundException {
        WorkspaceEntity workspace = workspaceRepository
                .findById(workspaceId)
                .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found by this id"));

        WorkspaceMemberEntity workspaceMember = workspaceMemberRepository
                .findByWorkspaceAndUser(workspace, user)
                .orElseThrow(() -> new WorkspaceNotFoundException("WorkspaceMember not found by this id"));

        return workspaceRoleMapper.toDto(workspaceMember.getRole());
    }

    public WorkspaceMemberRead createMember(UserEntity user,
                                      Long workspaceId,
                                      WorkspaceMemberCreate dto)
            throws WorkspaceNotFoundException, WorkspaceRoleInsuficientRightsExceptions {

        WorkspaceEntity workspace = workspaceRepository
                .findById(workspaceId)
                .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found by this id"));

        WorkspaceMemberEntity workspaceMember = workspaceMemberRepository
                .findByWorkspaceAndUser(workspace, user)
                .orElseThrow(() -> new WorkspaceNotFoundException("WorkspaceMember not found by this id"));

        if (!workspaceMember.getRole().isAbleToManageSettings()) {
            throw new WorkspaceRoleInsuficientRightsExceptions("You don't have permission to create members in this workspace");
        }

        WorkspaceMemberEntity member = workspaceMemberRepository
                .findByWorkspaceIdAndUserId(workspaceId, dto.getUserId())
                .orElseThrow(() -> new WorkspaceNotFoundException("WorkspaceMember not found by this id"));

        WorkspaceRoleEntity role = workspaceRoleRepository
                .findById(dto.getWorkspaceRoleId())
                .orElseThrow(() -> new WorkspaceNotFoundException("WorkspaceRole not found by this id"));

        member.setRole(role);

        return workspaceMemberMapper.toDto(member);
    }

    public WorkspaceRoleRead createRole(UserEntity user,
                                        WorkspaceRoleCreate dto)
            throws WorkspaceNotFoundException, WorkspaceRoleInsuficientRightsExceptions {

        WorkspaceEntity workspace = workspaceRepository
                .findById(dto.getWorkspaceId())
                .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found by this id"));

        WorkspaceMemberEntity workspaceMember = workspaceMemberRepository
                .findByWorkspaceAndUser(workspace, user)
                .orElseThrow(() -> new WorkspaceNotFoundException("WorkspaceMember not found by this id"));

        if (!workspaceMember.getRole().isAbleToManageSettings()) {
            throw new WorkspaceRoleInsuficientRightsExceptions("You don't have permission to create roles in this workspace");
        }

        WorkspaceRoleEntity role = workspaceRoleMapper.toEntity(dto);
        role.setWorkspace(workspace);

        return workspaceRoleMapper.toDto(workspaceRoleRepository.save(role));
    }

    public WorkspaceRoleRead updateRole(UserEntity user,
                                        Long id,
                                        WorkspaceRoleUpdate dto)
            throws WorkspaceNotFoundException, WorkspaceRoleInsuficientRightsExceptions {
        WorkspaceRoleEntity role = workspaceRoleRepository
                .findById(id)
                .orElseThrow(() -> new WorkspaceNotFoundException("WorkspaceRole not found by this id"));

        WorkspaceEntity workspace = workspaceRepository
                .findById(role.getWorkspace().getId())
                .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found by this id"));

        WorkspaceMemberEntity workspaceMember = workspaceMemberRepository
                .findByWorkspaceAndUser(workspace, user)
                .orElseThrow(() -> new WorkspaceNotFoundException("WorkspaceMember not found by this id"));

        if (!workspaceMember.getRole().isAbleToManageSettings()) {
            throw new WorkspaceRoleInsuficientRightsExceptions("You don't have permission to create roles in this workspace");
        }

        if (!role.isAbleToManageSettings()) {
            throw new WorkspaceRoleInsuficientRightsExceptions("You don't have permission to update this role");
        }

        workspaceRoleMapper.updateEntityFromDto(dto, role);

        return workspaceRoleMapper.toDto(role);
    }

}
