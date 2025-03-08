package com.kyut.ordo.task.service;

import java.time.LocalDateTime;
import java.util.List;

import com.kyut.ordo.task.dto.CardCreate;
import com.kyut.ordo.task.dto.CardRead;
import com.kyut.ordo.task.dto.CardWithItsListRead;
import com.kyut.ordo.task.entity.CardEntity;
import com.kyut.ordo.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.kyut.ordo.board.exception.InsufficientBoardPermissionsException;
import com.kyut.ordo.board.service.BoardPermissionService;
import com.kyut.ordo.task.entity.ListEntity;
import com.kyut.ordo.task.exception.InsufficientTaskPermissionsException;
import com.kyut.ordo.task.exception.TaskListNotFoundException;
import com.kyut.ordo.task.exception.TaskNotFoundException;
import com.kyut.ordo.task.mapper.TaskMapper;
import com.kyut.ordo.task.repository.TaskListRepository;
import com.kyut.ordo.task.repository.TaskRepository;
import com.kyut.ordo.user.UserEntity;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskListRepository taskListRepository;
    private final UserRepository userRepository;
    private final BoardPermissionService boardPermissionService;
    private final TaskMapper taskMapper;
    
    @Transactional()
    public List<CardRead> findAllByTaskList(UserEntity user, Long listId)
            throws TaskListNotFoundException, InsufficientBoardPermissionsException {
        ListEntity taskList = taskListRepository.findById(listId)
            .orElseThrow(() -> new TaskListNotFoundException("Task list not found with id: " + listId));
        
        if (!boardPermissionService.hasPermission(taskList.getBoard().getId(), user.getId(), "EDIT")) {
            throw new InsufficientBoardPermissionsException("User does not have permission to view tasks in this list");
        }
        
        List<CardEntity> tasks = taskRepository.findAllByTaskListOrderByPosition(taskList);
        return tasks.stream()
            .map(taskMapper::toDto)
            .toList();
    }
    
    @Transactional()
    public Page<CardRead> findAllByTaskList(UserEntity user, Long listId, Pageable pageable)
            throws TaskListNotFoundException, InsufficientBoardPermissionsException {
        ListEntity taskList = taskListRepository.findById(listId)
            .orElseThrow(() -> new TaskListNotFoundException("Task list not found with id: " + listId));
        
        if (!boardPermissionService.hasPermission(taskList.getBoard().getId(), user.getId(), "EDIT")) {
            throw new InsufficientBoardPermissionsException("User does not have permission to view tasks in this list");
        }
        
        return taskRepository.findAllByTaskList(taskList, pageable)
            .map(taskMapper::toDto);
    }
    
    @Transactional()
    public Page<CardWithItsListRead> findAllAssignedToUser(UserEntity user, Pageable pageable) {
        return taskRepository.findAllByAssignedTo(user, pageable)
            .map(taskMapper::toDtoWithItsList);
    }
    
    @Transactional()
    public CardWithItsListRead findById(UserEntity user, Long id)
            throws TaskNotFoundException, InsufficientTaskPermissionsException {
        CardEntity task = taskRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));
        
        if (!boardPermissionService.hasPermission(task.getTaskList().getBoard().getId(), user.getId(), "EDIT")) {
            throw new InsufficientTaskPermissionsException("User does not have permission to view this task");
        }
        
        return taskMapper.toDtoWithItsList(task);
    }
    
    @Transactional
    public CardWithItsListRead createTask(UserEntity user, CardCreate dto)
            throws TaskListNotFoundException, InsufficientTaskPermissionsException {
        ListEntity taskList = taskListRepository.findById(dto.getListId())
            .orElseThrow(() -> new TaskListNotFoundException("Task list not found with id: " + dto.getListId()));
        
        if (!boardPermissionService.hasPermission(taskList.getBoard().getId(), user.getId(), "CREATE_TASKS")) {
            throw new InsufficientTaskPermissionsException("User does not have permission to create tasks in this list");
        }
        
        if (dto.getPosition() == null) {
            Integer taskCount = taskRepository.countByTaskList(taskList);
            dto.setPosition(taskCount);
        }
        
        UserEntity assignedTo = null;
//        if (dto.getAssignedToId() != null) {
//            // In a real implementation, you would fetch the user from a UserService
//            assignedTo = new UserEntity();
//            assignedTo.setId(dto.getAssignedToId());
//        }

        if (dto.getAssignedToId() != null) {
            assignedTo = userRepository
                    .findById(dto.getAssignedToId())
                    .orElseThrow(() ->
                            new TaskNotFoundException("User not found with id: " + dto.getAssignedToId()));
        }
        
        CardEntity task = taskMapper.toEntity(dto, taskList, user, assignedTo);
        task.setCreatedAt(LocalDateTime.now());
        task = taskRepository.save(task);
        
        return taskMapper.toDtoWithItsList(task);
    }
    
    @Transactional
    public CardWithItsListRead updateTask(UserEntity user, Long id, CardCreate dto)
            throws TaskNotFoundException, InsufficientTaskPermissionsException {
        CardEntity task = taskRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));
        
        if (!boardPermissionService.hasPermission(task.getTaskList().getBoard().getId(), user.getId(), "EDIT")) {
            throw new InsufficientTaskPermissionsException("User does not have permission to edit this task");
        }
        
        // Don't allow changing the list if it's provided and different
        if (dto.getListId() != null && !dto.getListId().equals(task.getTaskList().getId())) {
            ListEntity newList = taskListRepository.findById(dto.getListId())
                .orElseThrow(() -> new TaskListNotFoundException("Task list not found with id: " + dto.getListId()));
            
            // Ensure the new list is in the same board
            if (!newList.getBoard().getId().equals(task.getTaskList().getBoard().getId())) {
                throw new IllegalArgumentException("Cannot move task to a list in a different board");
            }
            
            task.setTaskList(newList);
        }
        
        // Update assigned user if changed
        if (dto.getAssignedToId() != null) {
            if (task.getAssignedTo() == null || !dto.getAssignedToId().equals(task.getAssignedTo().getId())) {
                // In a real implementation, you would fetch the user from a UserService
                UserEntity assignedTo = new UserEntity();
                assignedTo.setId(dto.getAssignedToId());
                task.setAssignedTo(assignedTo);
            }
        } else if (task.getAssignedTo() != null) {
            // Remove assignment
            task.setAssignedTo(null);
        }
        
        taskMapper.updateEntityFromDto(dto, task);
        task = taskRepository.save(task);
        
        return taskMapper.toDtoWithItsList(task);
    }

    public CardWithItsListRead deleteTask(UserEntity user, Long id)
            throws TaskNotFoundException, InsufficientTaskPermissionsException {
        CardEntity task = taskRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));
        
        if (!boardPermissionService.hasPermission(task.getTaskList().getBoard().getId(), user.getId(), "EDIT")) {
            throw new InsufficientTaskPermissionsException("User does not have permission to delete this task");
        }

        taskRepository.delete(task);
        
        return taskMapper.toDtoWithItsList(task);
    }
}
