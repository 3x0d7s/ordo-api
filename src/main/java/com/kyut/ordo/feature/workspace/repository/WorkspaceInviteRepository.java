package com.kyut.ordo.feature.workspace.repository;

import com.kyut.ordo.feature.workspace.entity.WorkspaceInviteEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface WorkspaceInviteRepository extends CrudRepository<WorkspaceInviteEntity, Long> {
    Optional<WorkspaceInviteEntity> findByToken(String token);
}