package com.kyut.ordo.task.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.kyut.ordo.board.exception.InsufficientBoardPermissionsException;
import com.kyut.ordo.board.service.BoardPermissionService;
import com.kyut.ordo.card.entity.CardEntity;
import com.kyut.ordo.card.exception.CardNotFoundException;
import com.kyut.ordo.card.repository.CardRepository;
import com.kyut.ordo.task.dto.TaskCreate;
import com.kyut.ordo.task.dto.TaskRead;
import com.kyut.ordo.task.entity.TaskEntity;
import com.kyut.ordo.task.exception.InsufficientTaskPermissionsException;
import com.kyut.ordo.task.exception.TaskNotFoundException;
import com.kyut.ordo.task.mapper.TaskMapper;
import com.kyut.ordo.task.repository.TaskRepository;
import com.kyut.ordo.user.entity.UserEntity;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final CardRepository cardRepository;
    private final BoardPermissionService boardPermissionService;
    private final TaskMapper taskMapper;
    
    @Transactional
    public List<TaskRead> findAllByCard(UserEntity user, Long cardId)
            throws CardNotFoundException, InsufficientBoardPermissionsException {
        CardEntity card = cardRepository.findById(cardId)
            .orElseThrow(() -> new CardNotFoundException("Card not found with id: " + cardId));
        
        if (!boardPermissionService.hasPermission(card.getList().getBoard().getId(), user.getId(), "EDIT")) {
            throw new InsufficientBoardPermissionsException("User does not have permission to view tasks in this card");
        }
        
        List<TaskEntity> tasks = taskRepository.findAllByCardOrderByPosition(card);
        return tasks.stream()
            .map(taskMapper::toDto)
            .toList();
    }
    
    @Transactional
    public Page<TaskRead> findAllByCard(UserEntity user, Long cardId, Pageable pageable)
            throws CardNotFoundException, InsufficientBoardPermissionsException {
        CardEntity card = cardRepository.findById(cardId)
            .orElseThrow(() -> new CardNotFoundException("Card not found with id: " + cardId));
        
        if (!boardPermissionService.hasPermission(card.getList().getBoard().getId(), user.getId(), "EDIT")) {
            throw new InsufficientBoardPermissionsException("User does not have permission to view tasks in this card");
        }
        
        return taskRepository.findAllByCard(card, pageable)
            .map(taskMapper::toDto);
    }
    
    @Transactional
    public TaskRead findById(UserEntity user, Long id)
            throws TaskNotFoundException, InsufficientTaskPermissionsException {
        TaskEntity task = taskRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));
        
        if (!boardPermissionService.hasPermission(task.getCard().getList().getBoard().getId(), user.getId(), "EDIT")) {
            throw new InsufficientTaskPermissionsException("User does not have permission to view this task");
        }
        
        return taskMapper.toDto(task);
    }
    
    @Transactional
    public TaskRead createTask(UserEntity user, TaskCreate dto)
            throws CardNotFoundException, InsufficientTaskPermissionsException {
        CardEntity card = cardRepository.findById(dto.getCardId())
            .orElseThrow(() -> new CardNotFoundException("Card not found with id: " + dto.getCardId()));
        
        if (!boardPermissionService.hasPermission(card.getList().getBoard().getId(), user.getId(), "EDIT")) {
            throw new InsufficientTaskPermissionsException("User does not have permission to create tasks in this card");
        }
        
        if (dto.getPosition() == null) {
            Integer taskCount = taskRepository.countByCard(card);
            dto.setPosition(taskCount);
        }
        
        if (dto.getCompleted() == null) {
            dto.setCompleted(false);
        }
        
        TaskEntity task = taskMapper.toEntity(dto, card);
        task = taskRepository.save(task);
        
        return taskMapper.toDto(task);
    }
    
    @Transactional
    public TaskRead updateTask(UserEntity user, Long id, TaskCreate dto)
            throws TaskNotFoundException, InsufficientTaskPermissionsException, CardNotFoundException {
        TaskEntity task = taskRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));
        
        if (!boardPermissionService.hasPermission(task.getCard().getList().getBoard().getId(), user.getId(), "EDIT")) {
            throw new InsufficientTaskPermissionsException("User does not have permission to edit this task");
        }
        
        // Don't allow changing the card if it's provided and different
        if (dto.getCardId() != null && !dto.getCardId().equals(task.getCard().getId())) {
            CardEntity newCard = cardRepository.findById(dto.getCardId())
                .orElseThrow(() -> new CardNotFoundException("Card not found with id: " + dto.getCardId()));
            
            // Ensure the new card is in the same board
            if (!newCard.getList().getBoard().getId().equals(task.getCard().getList().getBoard().getId())) {
                throw new IllegalArgumentException("Cannot move task to a card in a different board");
            }
            
            task.setCard(newCard);
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
        
        if (!boardPermissionService.hasPermission(task.getCard().getList().getBoard().getId(), user.getId(), "EDIT")) {
            throw new InsufficientTaskPermissionsException("User does not have permission to delete this task");
        }

        TaskRead taskRead = taskMapper.toDto(task);
        taskRepository.delete(task);
        
        return taskRead;
    }
}
