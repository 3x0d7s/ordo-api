package com.kyut.ordo.feature.card.repository;

import java.util.List;
import java.util.Optional;

import com.kyut.ordo.feature.card.entity.CardEntity;
import com.kyut.ordo.feature.list.entity.ListEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.kyut.ordo.feature.user.entity.UserEntity;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface CardRepository extends CrudRepository<CardEntity, Long> {
    
    List<CardEntity> findAllByList(ListEntity list);
    
    List<CardEntity> findAllByListOrderByPosition(ListEntity taskList);

    Optional<CardEntity> findByIdAndList(Long id, ListEntity list);
    
    List<CardEntity> findAllByAssignedTo(UserEntity user);
    
    Page<CardEntity> findAllByAssignedTo(UserEntity user, Pageable pageable);

    Page<CardEntity> findAllByList(ListEntity taskList, Pageable pageable);

    Integer countByList(ListEntity taskList);

    void deleteAllByList(ListEntity list);

    @Modifying
    @Transactional
    @Query(
            value = "DELETE FROM tasks WHERE card_id = :id",
            nativeQuery = true
    )
    void deleteTasksByCardId(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query(
            value = "DELETE FROM comments WHERE card_id = :id",
            nativeQuery = true
    )
    void deleteCommentsByCardId(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query(
            value = "DELETE FROM cards WHERE id = :id",
            nativeQuery = true)
    void deleteById(@Param("id") Long id);
}
