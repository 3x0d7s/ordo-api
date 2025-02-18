package com.kyut.ordo.workspace.controller;

import com.kyut.ordo.user.UserEntity;
import com.kyut.ordo.workspace.exception.WorkspaceNotFoundException;
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
@RequestMapping("/workspace")
@RequiredArgsConstructor
public class WorkspaceController {
    private final WorkspaceService workspaceService;

    @GetMapping
    public ResponseEntity<Page<WorkspaceRead>> findAllByOwner(@AuthenticationPrincipal UserEntity user,
                                                              Pageable pageable) {
        return ResponseEntity.ok(workspaceService.findAllByOwner(user, pageable));
    }

    @PostMapping
    public ResponseEntity<WorkspaceRead> createWorkspace(@AuthenticationPrincipal UserEntity user,
                                                         WorkspaceCreate dto) {
        return ResponseEntity.ok(workspaceService.createWorkspace(user, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<WorkspaceRead> deleteWorkspace(@AuthenticationPrincipal UserEntity user, @PathVariable long id)
            throws WorkspaceNotFoundException {
        return ResponseEntity.ok(workspaceService.deleteWorkspace(user, id));
    }

}
