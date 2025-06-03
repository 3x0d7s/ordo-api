package com.kyut.ordo.workspace.repository;

import com.kyut.ordo.user.entity.UserEntity;
import com.kyut.ordo.workspace.entity.WorkspaceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface WorkspaceRepository extends CrudRepository<WorkspaceEntity, Long> {
    Page<WorkspaceEntity> findAll(Pageable pageable);

    Page<WorkspaceEntity> findAllByOwner(UserEntity user, Pageable pageable);
    
    @Query("SELECT DISTINCT w FROM WorkspaceEntity w " +
           "JOIN w.members m " +
           "WHERE m.user = :user")
    Page<WorkspaceEntity> findAllByMembersUser(UserEntity user, Pageable pageable);

    @Query("SELECT DISTINCT w FROM WorkspaceEntity w " +
           "JOIN w.members m " +
           "WHERE m.user = :user AND w.owner != :user")
    Page<WorkspaceEntity> findAllJoinedByMember(UserEntity user, Pageable pageable);
}