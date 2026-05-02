package com.kyut.ordo.feature.task.service;

import java.util.List;

import com.kyut.ordo.feature.task.event.TaskCreatedEvent;
import com.kyut.ordo.feature.task.event.TaskDeletedEvent;
import com.kyut.ordo.feature.task.event.TaskUpdatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import com.kyut.ordo.feature.task.dto.TaskWithItsCardRead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.kyut.ordo.feature.board.exception.InsufficientBoardPermissionsException;
import com.kyut.ordo.feature.card.entity.CardEntity;
import com.kyut.ordo.feature.card.exception.CardNotFoundException;
import com.kyut.ordo.feature.card.repository.CardRepository;
import com.kyut.ordo.feature.task.dto.TaskCreate;
import com.kyut.ordo.feature.task.dto.TaskRead;
import com.kyut.ordo.feature.task.entity.TaskEntity;
import com.kyut.ordo.feature.task.exception.InsufficientTaskPermissionsException;
import com.kyut.ordo.feature.task.exception.TaskNotFoundException;
import com.kyut.ordo.feature.task.mapper.TaskMapper;
import com.kyut.ordo.feature.task.repository.TaskRepository;
import com.kyut.ordo.feature.user.entity.UserEntity;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final CardRepository cardRepository;
    private final TaskMapper taskMapper;
    private final ApplicationEventPublisher eventPublisher;
    
    @Transactional
    @PreAuthorize("@featureAuthService.canViewCardTasks(#p1, authentication)")
    public List<TaskRead> findAllByCard(UserEntity user, Long cardId)
            throws CardNotFoundException, InsufficientBoardPermissionsException {
        CardEntity card = cardRepository.findById(cardId)
            .orElseThrow(() -> new CardNotFoundException("Card not found with id: " + cardId));

        List<TaskEntity> tasks = taskRepository.findAllByCardOrderByPosition(card);
        return tasks.stream()
            .map(taskMapper::toDto)
            .toList();
    }
    
    @Transactional
    @PreAuthorize("@featureAuthService.canViewCardTasks(#p1, authentication)")
    public Page<TaskRead> findAllByCard(UserEntity user, Long cardId, Pageable pageable)
            throws CardNotFoundException, InsufficientBoardPermissionsException {
        CardEntity card = cardRepository.findById(cardId)
            .orElseThrow(() -> new CardNotFoundException("Card not found with id: " + cardId));

        return taskRepository.findAllByCard(card, pageable)
            .map(taskMapper::toDto);
    }
    
    @Transactional
    @PreAuthorize("@featureAuthService.canAccessTask(#p1, authentication)")
    public TaskRead findById(UserEntity user, Long id)
            throws TaskNotFoundException, InsufficientTaskPermissionsException {
        TaskEntity task = taskRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));

        return taskMapper.toDto(task);
    }
    
    @Transactional
    @PreAuthorize("@featureAuthService.canCreateTask(#p1.cardId, authentication)")
    public TaskWithItsCardRead createTask(UserEntity user, TaskCreate dto)
            throws CardNotFoundException, InsufficientTaskPermissionsException {
        CardEntity card = cardRepository.findById(dto.getCardId())
            .orElseThrow(() -> new CardNotFoundException("Card not found with id: " + dto.getCardId()));

        if (dto.getPosition() == null) {
            Integer taskCount = taskRepository.countByCard(card);
            dto.setPosition(taskCount);
        }
        
        if (dto.getCompleted() == null) {
            dto.setCompleted(false);
        }
        
        TaskEntity task = taskMapper.toEntity(dto, card);
        task = taskRepository.save(task);

        TaskRead taskRead = taskMapper.toDto(task);
        
        // Publish task created event.
        eventPublisher.publishEvent(new TaskCreatedEvent(
            task.getId(),
            task.getCard().getId(),
            taskRead
        ));
        
        return taskMapper.toDtoWithItsCard(task);
    }
    
    @Transactional
    @PreAuthorize("@featureAuthService.canAccessTask(#p1, authentication)")
    public TaskRead updateTask(UserEntity user, Long id, TaskCreate dto)
            throws TaskNotFoundException, InsufficientTaskPermissionsException, CardNotFoundException {
        TaskEntity task = taskRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));

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

        TaskRead taskRead = taskMapper.toDto(task);
        
        // Publish task updated event.
        eventPublisher.publishEvent(new TaskUpdatedEvent(
            id,
            task.getCard().getId(),
            taskRead
        ));

        return taskRead;
    }

    @Transactional
    @PreAuthorize("@featureAuthService.canAccessTask(#p1, authentication)")
    public TaskRead deleteTask(UserEntity user, Long id)
            throws TaskNotFoundException, InsufficientTaskPermissionsException {
        TaskEntity task = taskRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));

        TaskRead taskRead = taskMapper.toDto(task);
        Long cardId = task.getCard().getId();
        
        taskRepository.delete(task);

        eventPublisher.publishEvent(new TaskDeletedEvent(
            id,
            cardId,
            taskRead
        ));
        
        return taskRead;
    }
}
