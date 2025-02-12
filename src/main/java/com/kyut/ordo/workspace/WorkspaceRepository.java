package com.kyut.ordo.workspace;

import com.kyut.ordo.user.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface WorkspaceRepository extends CrudRepository<WorkspaceEntity, Long> {
    Page<WorkspaceEntity> findAll(Pageable pageable);

    Page<WorkspaceEntity> findAllByOwner(UserEntity user, Pageable pageable);
}
