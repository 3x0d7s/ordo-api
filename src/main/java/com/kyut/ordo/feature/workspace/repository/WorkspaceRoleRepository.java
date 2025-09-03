package com.kyut.ordo.feature.workspace.repository;

import com.kyut.ordo.feature.workspace.entity.WorkspaceRoleEntity;
import com.kyut.ordo.feature.workspace.entity.WorkspaceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;


public interface WorkspaceRoleRepository extends CrudRepository<WorkspaceRoleEntity, Long> {
    void deleteAllByWorkspace(WorkspaceEntity workspace);

    Page<WorkspaceRoleEntity> findAllByWorkspace(WorkspaceEntity workspace, Pageable pageable);

    WorkspaceRoleEntity findByNameAndWorkspace(String name, WorkspaceEntity workspace);
}
