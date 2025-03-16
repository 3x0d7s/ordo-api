package com.kyut.ordo.workspace.controller;

import com.kyut.ordo.board.dto.BoardRead;
import com.kyut.ordo.board.service.BoardService;
import com.kyut.ordo.user.entity.UserEntity;
import com.kyut.ordo.workspace.dto.WorkspaceRoleRead;
import com.kyut.ordo.workspace.dto.WorkspaceUpdate;
import com.kyut.ordo.workspace.exception.WorkspaceNotFoundException;
import com.kyut.ordo.workspace.exception.WorkspaceRoleInsuficientRightsExceptions;
import com.kyut.ordo.workspace.service.WorkspaceService;
import com.kyut.ordo.workspace.dto.WorkspaceCreate;
import com.kyut.ordo.workspace.dto.WorkspaceRead;
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

    @GetMapping("/{id}/boards")
    public ResponseEntity<Page<BoardRead>> findBoardsByWorkspaceId(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable long id,
            Pageable pageable) {
        return ResponseEntity.ok(boardService.findAllBoardsByWorkspace(user, id, pageable));
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
