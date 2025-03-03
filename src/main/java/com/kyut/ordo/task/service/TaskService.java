package com.kyut.ordo.task.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kyut.ordo.board.exception.InsufficientBoardPermissionsException;
import com.kyut.ordo.board.service.BoardPermissionService;
import com.kyut.ordo.task.dto.TaskCreate;
import com.kyut.ordo.task.dto.TaskRead;
import com.kyut.ordo.task.entity.TaskEntity;
import com.kyut.ordo.task.entity.TaskListEntity;
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
    private final BoardPermissionService boardPermissionService;
    private final TaskMapper taskMapper;
    
    @Transactional(readOnly = true)
    public List<TaskRead> findAllByList(UserEntity user, Long listId) 
            throws TaskListNotFoundException, InsufficientBoardPermissionsException {
        TaskListEntity taskList = taskListRepository.findById(listId)
            .orElseThrow(() -> new TaskListNotFoundException("Task list not found with id: " + listId));
        
        if (!boardPermissionService.hasPermission(taskList.getBoard().getId(), user.getId(), "EDIT")) {
            throw new InsufficientBoardPermissionsException("User does not have permission to view tasks in this list");
        }
        
        List<TaskEntity> tasks = taskRepository.findAllByListOrderByPosition(taskList);
        return tasks.stream()
            .map(taskMapper::toDto)
            .toList();
    }
    
    @Transactional(readOnly = true)
    public Page<TaskRead> findAllByList(UserEntity user, Long listId, Pageable pageable) 
            throws TaskListNotFoundException, InsufficientBoardPermissionsException {
        TaskListEntity taskList = taskListRepository.findById(listId)
            .orElseThrow(() -> new TaskListNotFoundException("Task list not found with id: " + listId));
        
        if (!boardPermissionService.hasPermission(taskList.getBoard().getId(), user.getId(), "EDIT")) {
            throw new InsufficientBoardPermissionsException("User does not have permission to view tasks in this list");
        }
        
        return taskRepository.findAllByList(taskList, pageable)
            .map(taskMapper::toDto);
    }
    
    @Transactional(readOnly = true)
    public Page<TaskRead> findAllAssignedToUser(UserEntity user, Pageable pageable) {
        return taskRepository.findAllByAssignedTo(user, pageable)
            .map(taskMapper::toDto);
    }
    
    @Transactional(readOnly = true)
    public TaskRead findById(UserEntity user, Long id) 
            throws TaskNotFoundException, InsufficientTaskPermissionsException {
        TaskEntity task = taskRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));
        
        if (!boardPermissionService.hasPermission(task.getList().getBoard().getId(), user.getId(), "EDIT")) {
            throw new InsufficientTaskPermissionsException("User does not have permission to view this task");
        }
        
        return taskMapper.toDto(task);
    }
    
    @Transactional
    public TaskRead createTask(UserEntity user, TaskCreate dto) 
            throws TaskListNotFoundException, InsufficientTaskPermissionsException {
        TaskListEntity taskList = taskListRepository.findById(dto.getListId())
            .orElseThrow(() -> new TaskListNotFoundException("Task list not found with id: " + dto.getListId()));
        
        if (!boardPermissionService.hasPermission(taskList.getBoard().getId(), user.getId(), "CREATE_TASKS")) {
            throw new InsufficientTaskPermissionsException("User does not have permission to create tasks in this list");
        }
        
        // If position is not provided, add to the end
        if (dto.getPosition() == null) {
            Integer taskCount = taskRepository.countByList(taskList);
            dto.setPosition(taskCount);
        }
        
        // Handle assigned user
        UserEntity assignedTo = null;
        if (dto.getAssignedToId() != null) {
            // In a real implementation, you would fetch the user from a UserService
            assignedTo = new UserEntity();
            assignedTo.setId(dto.getAssignedToId());
        }
        
        TaskEntity task = taskMapper.toEntity(dto, taskList, user, assignedTo);
        task.setCreatedAt(LocalDateTime.now());
        task = taskRepository.save(task);
        
        return taskMapper.toDto(task);
    }
    
    @Transactional
    public TaskRead updateTask(UserEntity user, Long id, TaskCreate dto) 
            throws TaskNotFoundException, InsufficientTaskPermissionsException {
        TaskEntity task = taskRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));
        
        if (!boardPermissionService.hasPermission(task.getList().getBoard().getId(), user.getId(), "EDIT")) {
            throw new InsufficientTaskPermissionsException("User does not have permission to edit this task");
        }
        
        // Don't allow changing the list if it's provided and different
        if (dto.getListId() != null && !dto.getListId().equals(task.getList().getId())) {
            TaskListEntity newList = taskListRepository.findById(dto.getListId())
                .orElseThrow(() -> new TaskListNotFoundException("Task list not found with id: " + dto.getListId()));
            
            // Ensure the new list is in the same board
            if (!newList.getBoard().getId().equals(task.getList().getBoard().getId())) {
                throw new IllegalArgumentException("Cannot move task to a list in a different board");
            }
            
            task.setList(newList);
        }
        
        // Update assigned user if changed
        if (dto.getAssignedToId() != null) {
            if (task.getAssignedTo() == null || !dto.getAssignedToId().equals(task.getAssignedTo().getId())) {
                // In a real implementation, you would fetch the user from a UserService
                UserEntity assignedTo = new UserEntity();
                assignedTo.setId(dto.getAssignedToId());
                task.setAssignedTo(assignedTo);
            }
        } else if (dto.getAssignedToId() == null && task.getAssignedTo() != null) {
            // Remove assignment
            task.setAssignedTo(null);
        }
        
        taskMapper.updateEntityFromDto(dto, task);
        task = taskRepository.save(task);
        
        return taskMapper.toDto(task);
    }
    
    @Transactional
    public TaskRead deleteTask(UserEntity user, Long id) 
            throws TaskNotFoundException, InsufficientTaskPermissionsException {
        TaskEntity task = taskRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));
        
        if (!boardPermissionService.hasPermission(task.getList().getBoard().getId(), user.getId(), "EDIT")) {
            throw new InsufficientTaskPermissionsException("User does not have permission to delete this task");
        }
        
        taskRepository.delete(task);
        
        return taskMapper.toDto(task);
    }
}
