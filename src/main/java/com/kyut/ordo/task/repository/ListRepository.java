package com.kyut.ordo.task.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import com.kyut.ordo.board.entity.BoardEntity;
import com.kyut.ordo.task.entity.ListEntity;

public interface ListRepository extends CrudRepository<ListEntity, Long> {
    
    List<ListEntity> findAllByBoard(BoardEntity board);
    
    List<ListEntity> findAllByBoardOrderByPosition(BoardEntity board);
    
    Page<ListEntity> findAllByBoard(BoardEntity board, Pageable pageable);
    
    Optional<ListEntity> findByIdAndBoard(Long id, BoardEntity board);
    
    Integer countByBoard(BoardEntity board);
}
