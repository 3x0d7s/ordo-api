package com.kyut.ordo.task.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import com.kyut.ordo.task.entity.TaskEntity;
import com.kyut.ordo.task.entity.TaskListEntity;
import com.kyut.ordo.user.UserEntity;

public interface TaskRepository extends CrudRepository<TaskEntity, Long> {
    
    List<TaskEntity> findAllByTaskList(TaskListEntity list);
    
    List<TaskEntity> findAllByTaskListOrderByPosition(TaskListEntity taskList);

    Optional<TaskEntity> findByIdAndTaskList(Long id, TaskListEntity list);
    
    List<TaskEntity> findAllByAssignedTo(UserEntity user);
    
    Page<TaskEntity> findAllByAssignedTo(UserEntity user, Pageable pageable);

    Page<TaskEntity> findAllByTaskList(TaskListEntity taskList, Pageable pageable);

    Integer countByTaskList(TaskListEntity taskList);
}
