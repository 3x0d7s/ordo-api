package com.kyut.ordo.feature.task.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import com.kyut.ordo.feature.card.entity.CardEntity;
import com.kyut.ordo.feature.task.entity.TaskEntity;

public interface TaskRepository extends CrudRepository<TaskEntity, Long> {
    
    List<TaskEntity> findAllByCard(CardEntity card);
    
    List<TaskEntity> findAllByCardOrderByPosition(CardEntity card);

    Optional<TaskEntity> findByIdAndCard(Long id, CardEntity card);
    
    Page<TaskEntity> findAllByCard(CardEntity card, Pageable pageable);

    Integer countByCard(CardEntity card);
}
