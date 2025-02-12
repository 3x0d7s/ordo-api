package com.kyut.ordo.workspace;

import com.kyut.ordo.user.UserEntity;
import com.kyut.ordo.workspace.dto.WorkspaceCreate;
import com.kyut.ordo.workspace.dto.WorkspaceRead;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkspaceService {
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMapper workspaceMapper;

    public Page<WorkspaceRead> findAllByOwner(UserEntity user, Pageable pageable) {
        return workspaceRepository
                .findAllByOwner(user, pageable)
                .map(workspaceMapper::toDto);
    }

    public WorkspaceRead createWorkspace(UserEntity user, WorkspaceCreate dto) {
        WorkspaceEntity workspace = workspaceMapper.toEntity(dto);
        workspace.setOwner(user);
        workspaceRepository.save(workspace);
        return workspaceMapper.toDto(workspace);
    }

    public WorkspaceRead deleteWorkspace(UserEntity user, Long id) {
        WorkspaceEntity workspace = workspaceRepository.findById(id).orElseThrow();
        workspaceRepository.delete(workspace);
        return workspaceMapper.toDto(workspace);
    }

}
