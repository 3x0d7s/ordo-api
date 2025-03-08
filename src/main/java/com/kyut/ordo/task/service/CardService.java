package com.kyut.ordo.task.service;

import java.time.LocalDateTime;
import java.util.List;

import com.kyut.ordo.task.dto.CardCreate;
import com.kyut.ordo.task.dto.CardRead;
import com.kyut.ordo.task.dto.CardWithItsListRead;
import com.kyut.ordo.task.entity.CardEntity;
import com.kyut.ordo.task.mapper.CardMapper;
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
import com.kyut.ordo.task.repository.ListRepository;
import com.kyut.ordo.task.repository.CardRepository;
import com.kyut.ordo.user.UserEntity;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final ListRepository listRepository;
    private final UserRepository userRepository;
    private final BoardPermissionService boardPermissionService;
    private final CardMapper cardMapper;
    
    @Transactional()
    public List<CardRead> findAllByTaskList(UserEntity user, Long listId)
            throws TaskListNotFoundException, InsufficientBoardPermissionsException {
        ListEntity taskList = listRepository.findById(listId)
            .orElseThrow(() -> new TaskListNotFoundException("Task list not found with id: " + listId));
        
        if (!boardPermissionService.hasPermission(taskList.getBoard().getId(), user.getId(), "EDIT")) {
            throw new InsufficientBoardPermissionsException("User does not have permission to view tasks in this list");
        }
        
        List<CardEntity> tasks = cardRepository.findAllByTaskListOrderByPosition(taskList);
        return tasks.stream()
            .map(cardMapper::toDto)
            .toList();
    }
    
    @Transactional()
    public Page<CardRead> findAllByTaskList(UserEntity user, Long listId, Pageable pageable)
            throws TaskListNotFoundException, InsufficientBoardPermissionsException {
        ListEntity taskList = listRepository.findById(listId)
            .orElseThrow(() -> new TaskListNotFoundException("Task list not found with id: " + listId));
        
        if (!boardPermissionService.hasPermission(taskList.getBoard().getId(), user.getId(), "EDIT")) {
            throw new InsufficientBoardPermissionsException("User does not have permission to view tasks in this list");
        }
        
        return cardRepository.findAllByTaskList(taskList, pageable)
            .map(cardMapper::toDto);
    }
    
    @Transactional()
    public Page<CardWithItsListRead> findAllAssignedToUser(UserEntity user, Pageable pageable) {
        return cardRepository.findAllByAssignedTo(user, pageable)
            .map(cardMapper::toDtoWithItsList);
    }
    
    @Transactional()
    public CardWithItsListRead findById(UserEntity user, Long id)
            throws TaskNotFoundException, InsufficientTaskPermissionsException {
        CardEntity task = cardRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));
        
        if (!boardPermissionService.hasPermission(task.getTaskList().getBoard().getId(), user.getId(), "EDIT")) {
            throw new InsufficientTaskPermissionsException("User does not have permission to view this task");
        }
        
        return cardMapper.toDtoWithItsList(task);
    }
    
    @Transactional
    public CardWithItsListRead createTask(UserEntity user, CardCreate dto)
            throws TaskListNotFoundException, InsufficientTaskPermissionsException {
        ListEntity taskList = listRepository.findById(dto.getListId())
            .orElseThrow(() -> new TaskListNotFoundException("Task list not found with id: " + dto.getListId()));
        
        if (!boardPermissionService.hasPermission(taskList.getBoard().getId(), user.getId(), "CREATE_TASKS")) {
            throw new InsufficientTaskPermissionsException("User does not have permission to create tasks in this list");
        }
        
        if (dto.getPosition() == null) {
            Integer taskCount = cardRepository.countByTaskList(taskList);
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
        
        CardEntity task = cardMapper.toEntity(dto, taskList, user, assignedTo);
        task.setCreatedAt(LocalDateTime.now());
        task = cardRepository.save(task);
        
        return cardMapper.toDtoWithItsList(task);
    }
    
    @Transactional
    public CardWithItsListRead updateTask(UserEntity user, Long id, CardCreate dto)
            throws TaskNotFoundException, InsufficientTaskPermissionsException {
        CardEntity task = cardRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));
        
        if (!boardPermissionService.hasPermission(task.getTaskList().getBoard().getId(), user.getId(), "EDIT")) {
            throw new InsufficientTaskPermissionsException("User does not have permission to edit this task");
        }
        
        // Don't allow changing the list if it's provided and different
        if (dto.getListId() != null && !dto.getListId().equals(task.getTaskList().getId())) {
            ListEntity newList = listRepository.findById(dto.getListId())
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
        
        cardMapper.updateEntityFromDto(dto, task);
        task = cardRepository.save(task);
        
        return cardMapper.toDtoWithItsList(task);
    }

    public CardWithItsListRead deleteTask(UserEntity user, Long id)
            throws TaskNotFoundException, InsufficientTaskPermissionsException {
        CardEntity task = cardRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));
        
        if (!boardPermissionService.hasPermission(task.getTaskList().getBoard().getId(), user.getId(), "EDIT")) {
            throw new InsufficientTaskPermissionsException("User does not have permission to delete this task");
        }

        cardRepository.delete(task);
        
        return cardMapper.toDtoWithItsList(task);
    }
}
