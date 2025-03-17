package com.kyut.ordo.card.repository;

import java.util.List;
import java.util.Optional;

import com.kyut.ordo.card.entity.CardEntity;
import com.kyut.ordo.list.entity.ListEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import com.kyut.ordo.user.entity.UserEntity;

public interface CardRepository extends CrudRepository<CardEntity, Long> {
    
    List<CardEntity> findAllByList(ListEntity list);
    
    List<CardEntity> findAllByListOrderByPosition(ListEntity taskList);

    Optional<CardEntity> findByIdAndList(Long id, ListEntity list);
    
    List<CardEntity> findAllByAssignedTo(UserEntity user);
    
    Page<CardEntity> findAllByAssignedTo(UserEntity user, Pageable pageable);

    Page<CardEntity> findAllByList(ListEntity taskList, Pageable pageable);

    Integer countByList(ListEntity taskList);

    void deleteAllByList(ListEntity list);
}
