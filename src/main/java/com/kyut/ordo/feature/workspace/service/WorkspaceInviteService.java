package com.kyut.ordo.feature.workspace.service;

import com.kyut.ordo.feature.user.entity.UserEntity;
import com.kyut.ordo.feature.workspace.dto.WorkspaceInviteCreate;
import com.kyut.ordo.feature.workspace.dto.WorkspaceInviteRead;
import com.kyut.ordo.feature.workspace.dto.WorkspaceRead;
import com.kyut.ordo.feature.workspace.entity.WorkspaceEntity;
import com.kyut.ordo.feature.workspace.entity.WorkspaceInviteEntity;
import com.kyut.ordo.feature.workspace.entity.WorkspaceRoleEntity;
import com.kyut.ordo.feature.workspace.entity.WorkspaceMemberEntity;
import com.kyut.ordo.feature.workspace.exception.WorkspaceNotFoundException;
import com.kyut.ordo.feature.workspace.exception.WorkspaceRoleInsuficientRightsExceptions;
import com.kyut.ordo.feature.workspace.mapper.WorkspaceInviteMapper;
import com.kyut.ordo.feature.workspace.mapper.WorkspaceMapper;
import com.kyut.ordo.feature.workspace.repository.WorkspaceInviteRepository;
import com.kyut.ordo.feature.workspace.repository.WorkspaceRepository;
import com.kyut.ordo.feature.workspace.repository.WorkspaceRoleRepository;
import com.kyut.ordo.feature.workspace.repository.WorkspaceMemberRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class WorkspaceInviteService {
    private final WorkspaceInviteRepository inviteRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceRoleRepository roleRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final WorkspaceInviteMapper inviteMapper;
    private final WorkspaceMapper workspaceMapper;

    private static String generateSecureToken(int byteLength) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] token = new byte[byteLength];
        secureRandom.nextBytes(token);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token);
    }

    @Transactional
    @PreAuthorize("@featureAuthService.canManageWorkspaceSettings(#p1.workspaceId, authentication)")
    public WorkspaceInviteRead createInvite(UserEntity user, WorkspaceInviteCreate dto) 
            throws WorkspaceNotFoundException, WorkspaceRoleInsuficientRightsExceptions {
        WorkspaceEntity workspace = workspaceRepository
                .findById(dto.getWorkspaceId())
                .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found"));

        WorkspaceRoleEntity role = roleRepository
                .findById(dto.getRoleId())
                .orElseThrow(() -> new WorkspaceNotFoundException("WorkspaceRole not found"));

        String token = generateSecureToken(24);
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
    public WorkspaceRead acceptInvite(UserEntity user, String token)
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

        WorkspaceEntity workspace = invite.getWorkspace();
        WorkspaceRead workspaceRead = workspaceMapper.toDto(workspace);

        workspaceMemberRepository.save(member);
        inviteRepository.delete(invite);

        return workspaceRead;
    }
} 