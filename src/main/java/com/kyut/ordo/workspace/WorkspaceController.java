package com.kyut.ordo.workspace;

import com.kyut.ordo.user.UserEntity;
import com.kyut.ordo.workspace.dto.WorkspaceCreate;
import com.kyut.ordo.workspace.dto.WorkspaceRead;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/workspace")
@RequiredArgsConstructor
public class WorkspaceController {
    private final WorkspaceService workspaceService;

    @GetMapping
    public Page<WorkspaceRead> findAllByOwner(@AuthenticationPrincipal UserEntity user, Pageable pageable) {
        return workspaceService.findAllByOwner(user, pageable);
    }

    @PostMapping
    public WorkspaceRead createWorkspace(@AuthenticationPrincipal UserEntity user, WorkspaceCreate dto) {
        return workspaceService.createWorkspace(user, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteWorkspace(@AuthenticationPrincipal UserEntity user, @PathVariable Long id) {
        workspaceService.deleteWorkspace(user, id);
    }

}
