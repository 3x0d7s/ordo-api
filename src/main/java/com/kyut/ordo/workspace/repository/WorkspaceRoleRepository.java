package com.kyut.ordo.workspace.repository;

import com.kyut.ordo.workspace.entity.WorkspaceRoleEntity;
import com.kyut.ordo.workspace.entity.WorkspaceEntity;
import org.springframework.data.repository.CrudRepository;

public interface WorkspaceRoleRepository extends CrudRepository<WorkspaceRoleEntity, Long> {
    void deleteAllByWorkspace(WorkspaceEntity workspace);
}
