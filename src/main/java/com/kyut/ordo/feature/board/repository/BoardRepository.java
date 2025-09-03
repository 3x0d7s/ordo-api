package com.kyut.ordo.feature.board.repository;

import com.kyut.ordo.feature.board.entity.BoardEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface BoardRepository extends CrudRepository<BoardEntity, Long> {

    Page<BoardEntity> findAllByWorkspaceId(Long workspaceId, Pageable pageable);

    Page<BoardEntity> findAll(Pageable pageable);
}
