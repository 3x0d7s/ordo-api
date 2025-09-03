package com.kyut.ordo.feature.workspace.controller;

import com.kyut.ordo.feature.user.entity.UserEntity;
import com.kyut.ordo.feature.workspace.dto.WorkspaceInviteCreate;
import com.kyut.ordo.feature.workspace.dto.WorkspaceInviteRead;
import com.kyut.ordo.feature.workspace.dto.WorkspaceRead;
import com.kyut.ordo.feature.workspace.exception.WorkspaceNotFoundException;
import com.kyut.ordo.feature.workspace.exception.WorkspaceRoleInsuficientRightsExceptions;
import com.kyut.ordo.feature.workspace.service.WorkspaceInviteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/workspaces/invites")
@RequiredArgsConstructor
public class WorkspaceInviteController {
    private final WorkspaceInviteService inviteService;

    @PostMapping
    public ResponseEntity<WorkspaceInviteRead> createInvite(
            @AuthenticationPrincipal UserEntity user,
            @RequestBody WorkspaceInviteCreate dto) 
            throws WorkspaceNotFoundException, WorkspaceRoleInsuficientRightsExceptions {
        return ResponseEntity.ok(inviteService.createInvite(user, dto));
    }

    @PostMapping("/{token}/accept")
    public ResponseEntity<WorkspaceRead> acceptInvite(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable String token)
            throws WorkspaceNotFoundException, WorkspaceRoleInsuficientRightsExceptions {
        WorkspaceRead workspace = inviteService.acceptInvite(user, token);
        return ResponseEntity.ok(workspace);
    }
} 