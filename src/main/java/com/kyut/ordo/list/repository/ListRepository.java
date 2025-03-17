package com.kyut.ordo.list.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.kyut.ordo.board.entity.BoardEntity;
import com.kyut.ordo.list.entity.ListEntity;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ListRepository extends CrudRepository<ListEntity, Long> {
    
    List<ListEntity> findAllByBoard(BoardEntity board);
    
    List<ListEntity> findAllByBoardOrderByPosition(BoardEntity board);
    
    Page<ListEntity> findAllByBoard(BoardEntity board, Pageable pageable);
    
    Optional<ListEntity> findByIdAndBoard(Long id, BoardEntity board);
    
    Integer countByBoard(BoardEntity board);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM cards WHERE list_id = :listId", nativeQuery = true)
    void deleteCardsByListId(@Param("listId") Long listId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM lists WHERE id = :listId", nativeQuery = true)
    void deleteListById(@Param("listId") Long listId);
}
