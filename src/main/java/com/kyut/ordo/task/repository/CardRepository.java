package com.kyut.ordo.task.repository;

import java.util.List;
import java.util.Optional;

import com.kyut.ordo.task.entity.CardEntity;
import com.kyut.ordo.task.entity.ListEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import com.kyut.ordo.user.UserEntity;

public interface CardRepository extends CrudRepository<CardEntity, Long> {
    
    List<CardEntity> findAllByTaskList(ListEntity list);
    
    List<CardEntity> findAllByTaskListOrderByPosition(ListEntity taskList);

    Optional<CardEntity> findByIdAndTaskList(Long id, ListEntity list);
    
    List<CardEntity> findAllByAssignedTo(UserEntity user);
    
    Page<CardEntity> findAllByAssignedTo(UserEntity user, Pageable pageable);

    Page<CardEntity> findAllByTaskList(ListEntity taskList, Pageable pageable);

    Integer countByTaskList(ListEntity taskList);
}
