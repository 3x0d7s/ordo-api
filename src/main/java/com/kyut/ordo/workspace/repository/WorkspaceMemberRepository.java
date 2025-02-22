package com.kyut.ordo.workspace.repository;

import com.kyut.ordo.user.UserEntity;
import com.kyut.ordo.workspace.entity.WorkspaceEntity;
import com.kyut.ordo.workspace.entity.WorkspaceMemberEntity;
import org.springframework.data.repository.CrudRepository;

public interface WorkspaceMemberRepository extends CrudRepository<WorkspaceMemberEntity, Long> {
    WorkspaceMemberEntity findByWorkspaceAndUser(WorkspaceEntity workspace, UserEntity user);

    void deleteAllByWorkspace(WorkspaceEntity workspace);
}
