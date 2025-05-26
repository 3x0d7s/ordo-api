package com.kyut.ordo.workspace.service;

import com.kyut.ordo.user.entity.UserEntity;
import com.kyut.ordo.workspace.dto.WorkspaceInviteCreate;
import com.kyut.ordo.workspace.dto.WorkspaceInviteRead;
import com.kyut.ordo.workspace.entity.WorkspaceEntity;
import com.kyut.ordo.workspace.entity.WorkspaceInviteEntity;
import com.kyut.ordo.workspace.entity.WorkspaceRoleEntity;
import com.kyut.ordo.workspace.entity.WorkspaceMemberEntity;
import com.kyut.ordo.workspace.exception.WorkspaceNotFoundException;
import com.kyut.ordo.workspace.exception.WorkspaceRoleInsuficientRightsExceptions;
import com.kyut.ordo.workspace.mapper.WorkspaceInviteMapper;
import com.kyut.ordo.workspace.repository.WorkspaceInviteRepository;
import com.kyut.ordo.workspace.repository.WorkspaceRepository;
import com.kyut.ordo.workspace.repository.WorkspaceRoleRepository;
import com.kyut.ordo.workspace.repository.WorkspaceMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkspaceInviteService {
    private final WorkspaceInviteRepository inviteRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceRoleRepository roleRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final WorkspaceInviteMapper inviteMapper;

    @Transactional
    public WorkspaceInviteRead createInvite(UserEntity user, WorkspaceInviteCreate dto) 
            throws WorkspaceNotFoundException, WorkspaceRoleInsuficientRightsExceptions {
        WorkspaceEntity workspace = workspaceRepository
                .findById(dto.getWorkspaceId())
                .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found"));

        WorkspaceRoleEntity role = roleRepository
                .findById(dto.getRoleId())
                .orElseThrow(() -> new WorkspaceNotFoundException("WorkspaceRole not found"));

        WorkspaceMemberEntity workspaceMember = workspaceMemberRepository
                .findByWorkspaceAndUser(workspace, user)
                .orElseThrow(() -> new WorkspaceNotFoundException("WorkspaceMember not found by this id"));

        if (!workspaceMember.getRole().isAbleToManageSettings()) {
            throw new WorkspaceRoleInsuficientRightsExceptions("You don't have permission to delete this workspace");
        }

        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(dto.getExpiresInDays());

        WorkspaceInviteEntity invite = WorkspaceInviteEntity.builder()
                .workspace(workspace)
                .createdBy(user)
                .token(token)
                .role(role)
                .expiresAt(expiresAt)
                .build();

        return inviteMapper.toDto(inviteRepository.save(invite));
    }

    @Transactional
    public void acceptInvite(UserEntity user, String token) 
            throws WorkspaceNotFoundException, WorkspaceRoleInsuficientRightsExceptions {
        WorkspaceInviteEntity invite = inviteRepository
                .findByToken(token)
                .orElseThrow(() -> new WorkspaceNotFoundException("Invalid or expired invite"));

        if (invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new WorkspaceRoleInsuficientRightsExceptions("Invite has expired");
        }

        // Add user to workspace with specified role
        WorkspaceMemberEntity member = WorkspaceMemberEntity.builder()
                .workspace(invite.getWorkspace())
                .user(user)
                .role(invite.getRole())
                .build();

        workspaceMemberRepository.save(member);
        inviteRepository.delete(invite);
    }
} 