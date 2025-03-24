package com.kyut.ordo.workspace.controller;

import com.kyut.ordo.board.dto.BoardRead;
import com.kyut.ordo.board.service.BoardService;
import com.kyut.ordo.user.entity.UserEntity;
import com.kyut.ordo.workspace.dto.*;
import com.kyut.ordo.workspace.exception.WorkspaceNotFoundException;
import com.kyut.ordo.workspace.exception.WorkspaceRoleInsuficientRightsExceptions;
import com.kyut.ordo.workspace.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {
    private final WorkspaceService workspaceService;
    private final BoardService boardService;

    @GetMapping
    public ResponseEntity<Page<WorkspaceRead>> findAllByOwner(@AuthenticationPrincipal UserEntity user,
                                                              Pageable pageable) {
        return ResponseEntity.ok(workspaceService.findAllByOwner(user, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkspaceRead> findById(@AuthenticationPrincipal UserEntity user,
                                                  @PathVariable long id)
            throws WorkspaceNotFoundException {
        return ResponseEntity.ok(workspaceService.findById(user, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkspaceRead> updateWorkspace(@AuthenticationPrincipal UserEntity user,
                                                         @PathVariable long id,
                                                         WorkspaceUpdate dto)
            throws WorkspaceNotFoundException, WorkspaceRoleInsuficientRightsExceptions {
        return ResponseEntity.ok(workspaceService.updateWorkspace(user, id, dto));
    }

    @GetMapping("/{id}/roles")
    public ResponseEntity<Page<WorkspaceRoleRead>> findRolesByWorkspaceId(@AuthenticationPrincipal UserEntity user,
                                                                          @PathVariable long id,
                                                                          Pageable pageable)
            throws WorkspaceNotFoundException {
        return ResponseEntity.ok(workspaceService.findRolesByWorkspaceId(id, pageable));
    }

    @PostMapping("/{id}/roles")
    public ResponseEntity<WorkspaceRoleRead> createRole(@AuthenticationPrincipal UserEntity user,
                                                        @PathVariable long id,
                                                        WorkspaceRoleCreate dto)
            throws WorkspaceNotFoundException, WorkspaceRoleInsuficientRightsExceptions {
        return ResponseEntity.ok(workspaceService.createRole(user, dto));
    }

    @PutMapping("/{id}/roles/{roleId}")
    public ResponseEntity<WorkspaceRoleRead> updateRole(@AuthenticationPrincipal UserEntity user,
                                                        @PathVariable long id,
                                                        @PathVariable long roleId,
                                                        WorkspaceRoleUpdate dto)
            throws WorkspaceNotFoundException, WorkspaceRoleInsuficientRightsExceptions {
        return ResponseEntity.ok(workspaceService.updateRole(user, roleId, dto));
    }

    @GetMapping("/{id}/boards")
    public ResponseEntity<Page<BoardRead>> findBoardsByWorkspaceId(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable long id,
            Pageable pageable) {
        return ResponseEntity.ok(boardService.findAllBoardsByWorkspace(user, id, pageable));
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<Page<WorkspaceMemberRead>> findMembersByWorkspaceId(@AuthenticationPrincipal UserEntity user,
                                                                              @PathVariable long id,
                                                                              Pageable pageable)
            throws WorkspaceNotFoundException {
        return ResponseEntity.ok(workspaceService.findMembersByWorkspaceId(id, pageable));
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<WorkspaceMemberRead> createMember(@AuthenticationPrincipal UserEntity user,
                                                            @PathVariable long id,
                                                            WorkspaceMemberCreate dto)
            throws WorkspaceNotFoundException, WorkspaceRoleInsuficientRightsExceptions {
        return ResponseEntity.ok(workspaceService.createMember(user, id, dto));
    }

    @PutMapping("/{id}/members/{memberId}")
    public ResponseEntity<WorkspaceMemberRead> updateMember(@AuthenticationPrincipal UserEntity user,
                                                            @PathVariable long id,
                                                            @PathVariable long memberId,
                                                            WorkspaceMemberUpdate dto)
            throws WorkspaceNotFoundException, WorkspaceRoleInsuficientRightsExceptions {
        return ResponseEntity.ok(workspaceService.updateMember(user, id, memberId, dto));
    }

    @PostMapping
    public ResponseEntity<WorkspaceRead> createWorkspace(@AuthenticationPrincipal UserEntity user,
                                                         WorkspaceCreate dto) {
        return ResponseEntity.ok(workspaceService.createWorkspace(user, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<WorkspaceRead> deleteWorkspace(@AuthenticationPrincipal UserEntity user,
                                                         @PathVariable long id)
            throws WorkspaceNotFoundException, WorkspaceRoleInsuficientRightsExceptions {
        return ResponseEntity.ok(workspaceService.deleteWorkspace(user, id));
    }

}
