package com.kyut.ordo.workspace.repository;

import com.kyut.ordo.workspace.entity.WorkspaceInviteEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface WorkspaceInviteRepository extends CrudRepository<WorkspaceInviteEntity, Long> {
    Optional<WorkspaceInviteEntity> findByToken(String token);
    void deleteAllByWorkspaceId(Long workspaceId);
} 