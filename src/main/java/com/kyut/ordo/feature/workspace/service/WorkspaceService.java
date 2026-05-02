package com.kyut.ordo.feature.workspace.service;

import com.kyut.ordo.feature.user.entity.UserEntity;

import com.kyut.ordo.feature.workspace.dto.WorkspaceCreate;
import com.kyut.ordo.feature.workspace.dto.WorkspaceMemberCreate;
import com.kyut.ordo.feature.workspace.dto.WorkspaceMemberRead;
import com.kyut.ordo.feature.workspace.dto.WorkspaceMemberUpdate;
import com.kyut.ordo.feature.workspace.dto.WorkspaceRead;
import com.kyut.ordo.feature.workspace.dto.WorkspaceRoleCreate;
import com.kyut.ordo.feature.workspace.dto.WorkspaceRoleRead;
import com.kyut.ordo.feature.workspace.dto.WorkspaceRoleUpdate;
import com.kyut.ordo.feature.workspace.dto.WorkspaceUpdate;
import com.kyut.ordo.feature.workspace.entity.WorkspaceEntity;
import com.kyut.ordo.feature.workspace.entity.WorkspaceRoleEntity;
import com.kyut.ordo.feature.workspace.entity.WorkspaceMemberEntity;
import com.kyut.ordo.feature.workspace.exception.WorkspaceNotFoundException;
import com.kyut.ordo.feature.workspace.exception.WorkspaceRoleInsuficientRightsExceptions;
import com.kyut.ordo.feature.workspace.mapper.WorkspaceMapper;
import com.kyut.ordo.feature.workspace.mapper.WorkspaceMemberMapper;
import com.kyut.ordo.feature.workspace.mapper.WorkspaceRoleMapper;
import com.kyut.ordo.feature.workspace.repository.WorkspaceMemberRepository;
import com.kyut.ordo.feature.workspace.repository.WorkspaceRepository;
import com.kyut.ordo.feature.workspace.repository.WorkspaceRoleRepository;
import com.kyut.ordo.feature.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class WorkspaceService {
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceRoleRepository workspaceRoleRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final WorkspaceRoleFactory workspaceRoleFactory;
    private final UserRepository userRepository;

    private final WorkspaceRoleMapper workspaceRoleMapper;
    private final WorkspaceMemberMapper workspaceMemberMapper;
    private final WorkspaceMapper workspaceMapper;

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

    public Page<WorkspaceRead> findAllJoinedByMember(UserEntity user, Pageable pageable) {
        return workspaceRepository
                .findAllJoinedByMember(user, pageable)
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

    @PreAuthorize("@featureAuthService.canManageWorkspaceSettings(#p1, authentication)")
    public WorkspaceRead deleteWorkspace(UserEntity user, Long id)
            throws WorkspaceNotFoundException, WorkspaceRoleInsuficientRightsExceptions {
        WorkspaceEntity workspace = workspaceRepository
                .findById(id)
                .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found by this id"));

        workspaceRepository.delete(workspace);

        return workspaceMapper.toDto(workspace);
    }

    @PreAuthorize("@featureAuthService.canManageWorkspaceSettings(#p1, authentication)")
    public WorkspaceRead updateWorkspace(
            UserEntity user,
            Long id,
            WorkspaceUpdate dto) throws WorkspaceNotFoundException, WorkspaceRoleInsuficientRightsExceptions {
        WorkspaceEntity workspace = workspaceRepository
                .findById(id)
                .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found by this id"));

        workspaceMapper.updateEntityFromDto(dto, workspace);
        workspace = workspaceRepository.save(workspace);

        return workspaceMapper.toDto(workspace);
    }

    @Transactional
    @PreAuthorize("@featureAuthService.canManageWorkspaceSettings(#p1, authentication)")
    public WorkspaceMemberRead updateMember(UserEntity user,
                                      Long workspaceId,
                                      Long userId,
                                      WorkspaceMemberUpdate dto) throws WorkspaceNotFoundException, WorkspaceRoleInsuficientRightsExceptions {

        workspaceRepository
                .findById(workspaceId)
                .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found by this id"));

        WorkspaceMemberEntity memberToUpdate = workspaceMemberRepository
                .findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new WorkspaceNotFoundException("WorkspaceMember not found by this id"));

        WorkspaceRoleEntity role = workspaceRoleRepository
                .findById(dto.getWorkspaceRoleId())
                .orElseThrow(() -> new WorkspaceNotFoundException("WorkspaceRole not found by this id"));

        memberToUpdate.setRole(role);
        
        WorkspaceMemberEntity savedMember = workspaceMemberRepository.save(memberToUpdate);

        return workspaceMemberMapper.toDto(savedMember);
    }

    @PreAuthorize("@featureAuthService.canManageWorkspaceSettingsOrSelf(#p1, #p2, authentication)")
    public WorkspaceMemberRead deleteMember(UserEntity user,
                                      Long workspaceId,
                                      Long userId) throws WorkspaceNotFoundException, WorkspaceRoleInsuficientRightsExceptions {

        workspaceRepository
                .findById(workspaceId)
                .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found by this id"));

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

    @Transactional
    @PreAuthorize("@featureAuthService.canManageWorkspaceSettings(#p1, authentication)")
    public WorkspaceMemberRead createMember(UserEntity user,
                                      Long workspaceId,
                                      WorkspaceMemberCreate dto)
            throws WorkspaceNotFoundException, WorkspaceRoleInsuficientRightsExceptions {

        WorkspaceEntity workspace = workspaceRepository
                .findById(workspaceId)
                .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found by this id"));

        WorkspaceRoleEntity role = workspaceRoleRepository
                .findById(dto.getWorkspaceRoleId())
                .orElseThrow(() -> new WorkspaceNotFoundException("WorkspaceRole not found by this id"));

        WorkspaceMemberEntity member = workspaceMemberRepository
                .findByWorkspaceIdAndUserId(workspaceId, dto.getUserId())
                .orElse(null);

        if (member == null) {
            var newUser = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new WorkspaceNotFoundException("User not found by this id"));
            member = WorkspaceMemberEntity.builder()
                    .workspace(workspace)
                    .user(newUser)
                    .role(role)
                    .build();
        } else {
            // If member already existed, update their role
            member.setRole(role);
        }

        WorkspaceMemberEntity savedMember = workspaceMemberRepository.save(member);

        return workspaceMemberMapper.toDto(savedMember);
    }

    @PreAuthorize("@featureAuthService.canManageWorkspaceSettings(#p1.workspaceId, authentication)")
    public WorkspaceRoleRead createRole(UserEntity user,
                                        WorkspaceRoleCreate dto)
            throws WorkspaceNotFoundException, WorkspaceRoleInsuficientRightsExceptions {

        WorkspaceEntity workspace = workspaceRepository
                .findById(dto.getWorkspaceId())
                .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found by this id"));

        WorkspaceRoleEntity role = workspaceRoleMapper.toEntity(dto);
        role.setWorkspace(workspace);

        return workspaceRoleMapper.toDto(workspaceRoleRepository.save(role));
    }

    @PreAuthorize("@featureAuthService.canManageWorkspaceSettingsByRoleId(#p1, authentication)")
    public WorkspaceRoleRead updateRole(UserEntity user,
                                        Long id,
                                        WorkspaceRoleUpdate dto)
            throws WorkspaceNotFoundException, WorkspaceRoleInsuficientRightsExceptions {
        WorkspaceRoleEntity role = workspaceRoleRepository
                .findById(id)
                .orElseThrow(() -> new WorkspaceNotFoundException("WorkspaceRole not found by this id"));

        workspaceRoleMapper.updateEntityFromDto(dto, role);
        workspaceRoleRepository.save(role);

        return workspaceRoleMapper.toDto(role);
    }

}
