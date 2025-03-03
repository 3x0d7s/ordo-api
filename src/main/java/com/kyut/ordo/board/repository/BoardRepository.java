package com.kyut.ordo.board.repository;

import com.kyut.ordo.board.entity.BoardEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface BoardRepository extends CrudRepository<BoardEntity, Long> {

    void findAllByWorkspaceId(Long workspaceId);

    Page<BoardEntity> findAll(Pageable pageable);
}
