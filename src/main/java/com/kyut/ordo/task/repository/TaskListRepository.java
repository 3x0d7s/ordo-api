package com.kyut.ordo.task.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import com.kyut.ordo.board.entity.BoardEntity;
import com.kyut.ordo.task.entity.TaskListEntity;

public interface TaskListRepository extends CrudRepository<TaskListEntity, Long> {
    
    List<TaskListEntity> findAllByBoard(BoardEntity board);
    
    List<TaskListEntity> findAllByBoardOrderByPosition(BoardEntity board);
    
    Page<TaskListEntity> findAllByBoard(BoardEntity board, Pageable pageable);
    
    Optional<TaskListEntity> findByIdAndBoard(Long id, BoardEntity board);
    
    Integer countByBoard(BoardEntity board);
}
