package com.kyut.ordo.feature.workspace.repository;

import com.kyut.ordo.feature.user.entity.UserEntity;
import com.kyut.ordo.feature.workspace.entity.WorkspaceEntity;
import com.kyut.ordo.feature.workspace.entity.WorkspaceMemberEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface WorkspaceMemberRepository extends CrudRepository<WorkspaceMemberEntity, Long> {
    Optional<WorkspaceMemberEntity> findByWorkspaceAndUser(WorkspaceEntity workspace, UserEntity user);

    void deleteAllByWorkspace(WorkspaceEntity workspace);

    Optional<WorkspaceMemberEntity> findByWorkspaceIdAndUserId(Long workspaceId, Long id);

    Page<WorkspaceMemberEntity> findAllByWorkspace(WorkspaceEntity workspace, Pageable pageable);

    @Modifying
    @Transactional
    @Query(value =
            "insert INTO WorkspaceMemberEntity (workspace_id, user_id, role_id) values (?1, ?2, ?3)", nativeQuery = true)
    void addMember(Long workspaceId, Long userId, Long roleId);
}
