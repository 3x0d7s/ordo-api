package com.kyut.ordo.card.service;

import java.util.List;

import com.kyut.ordo.card.dto.CardCreate;
import com.kyut.ordo.card.dto.CardRead;
import com.kyut.ordo.card.dto.CardWithItsListRead;
import com.kyut.ordo.card.entity.CardEntity;
import com.kyut.ordo.card.event.CardCreatedEvent;
import com.kyut.ordo.card.event.CardDeletedEvent;
import com.kyut.ordo.card.event.CardPositionsUpdatedEvent;
import com.kyut.ordo.card.event.CardUpdatedEvent;
import com.kyut.ordo.card.mapper.CardMapper;
import com.kyut.ordo.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.kyut.ordo.board.exception.InsufficientBoardPermissionsException;
import com.kyut.ordo.board.service.BoardPermissionService;
import com.kyut.ordo.list.entity.ListEntity;
import com.kyut.ordo.card.exception.InsufficientCardPermissionsException;
import com.kyut.ordo.list.exception.ListNotFoundException;
import com.kyut.ordo.card.exception.CardNotFoundException;
import com.kyut.ordo.list.repository.ListRepository;
import com.kyut.ordo.card.repository.CardRepository;
import com.kyut.ordo.user.entity.UserEntity;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final ListRepository listRepository;
    private final UserRepository userRepository;
    private final BoardPermissionService boardPermissionService;
    private final CardMapper cardMapper;
    //    private final WebSocketService webSocketService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional()
    public List<CardRead> findAllByList(UserEntity user, Long listId)
            throws ListNotFoundException, InsufficientBoardPermissionsException {
        ListEntity taskList = listRepository.findById(listId)
            .orElseThrow(() -> new ListNotFoundException("Task list not found with id: " + listId));
        
//        if (!boardPermissionService.hasPermission(taskList.getBoard().getId(), user.getId(), "EDIT")) {
//            throw new InsufficientBoardPermissionsException("User does not have permission to view tasks in this list");
//        }
        
        List<CardEntity> tasks = cardRepository.findAllByListOrderByPosition(taskList);
        return tasks.stream()
            .map(cardMapper::toDto)
            .toList();
    }
    
    @Transactional()
    public Page<CardRead> findAllByList(UserEntity user, Long listId, Pageable pageable)
            throws ListNotFoundException, InsufficientBoardPermissionsException {
        ListEntity taskList = listRepository.findById(listId)
            .orElseThrow(() -> new ListNotFoundException("Task list not found with id: " + listId));
        
//        if (!boardPermissionService.hasPermission(taskList.getBoard().getId(), user.getId(), "EDIT")) {
//            throw new InsufficientBoardPermissionsException("User does not have permission to view tasks in this list");
//        }
        
        return cardRepository.findAllByList(taskList, pageable)
            .map(cardMapper::toDto);
    }
    
    @Transactional()
    public Page<CardWithItsListRead> findAllAssignedToUser(UserEntity user, Pageable pageable) {
        return cardRepository.findAllByAssignedTo(user, pageable)
            .map(cardMapper::toDtoWithItsList);
    }
    
    @Transactional()
    public CardWithItsListRead findById(UserEntity user, Long id)
            throws CardNotFoundException, InsufficientCardPermissionsException {
        CardEntity task = cardRepository.findById(id)
            .orElseThrow(() -> new CardNotFoundException("Task not found with id: " + id));
        
        if (!boardPermissionService.hasPermission(task.getList().getBoard().getId(), user.getId(), "EDIT")) {
            throw new InsufficientCardPermissionsException("User does not have permission to view this task");
        }
        
        return cardMapper.toDtoWithItsList(task);
    }
    
    @Transactional
    public CardWithItsListRead createCard(UserEntity user, CardCreate dto)
            throws ListNotFoundException, InsufficientCardPermissionsException {
        ListEntity list = listRepository.findById(dto.getListId())
            .orElseThrow(() -> new ListNotFoundException("Task list not found with id: " + dto.getListId()));
        
        if (!boardPermissionService.hasPermission(list.getBoard().getId(), user.getId(), "CREATE_TASKS")) {
            throw new InsufficientCardPermissionsException("User does not have permission to create tasks in this list");
        }
        
        if (dto.getPosition() == null) {
            Integer taskCount = cardRepository.countByList(list);
            dto.setPosition(taskCount);
        }
        
        UserEntity assignedTo = null;

        if (dto.getAssignedToId() != null) {
            assignedTo = userRepository
                    .findById(dto.getAssignedToId())
                    .orElseThrow(() ->
                            new CardNotFoundException("User not found with id: " + dto.getAssignedToId()));
        }
        
        CardEntity card = cardMapper.toEntity(dto, list, user, assignedTo);
        card = cardRepository.save(card);
        
        CardWithItsListRead result = cardMapper.toDtoWithItsList(card);

        eventPublisher.publishEvent(new CardCreatedEvent(
                card.getId(),
                list.getId(),
                list.getBoard().getId(),
                result)
        );

        return result;
    }
    
    @Transactional
    public CardWithItsListRead updateCard(UserEntity user, Long id, CardCreate dto)
            throws CardNotFoundException, InsufficientCardPermissionsException {
        CardEntity card = cardRepository.findById(id)
            .orElseThrow(() -> new CardNotFoundException("Task not found with id: " + id));
        
        if (!boardPermissionService.hasPermission(card.getList().getBoard().getId(), user.getId(), "EDIT")) {
            throw new InsufficientCardPermissionsException("User does not have permission to edit this card");
        }

        // Don't allow changing the list if it's provided and different
        if (dto.getListId() != null && !dto.getListId().equals(card.getList().getId())) {
            ListEntity newList = listRepository.findById(dto.getListId())
                .orElseThrow(() -> new ListNotFoundException("Task list not found with id: " + dto.getListId()));
            
            // Ensure the new list is in the same board
            if (!newList.getBoard().getId().equals(card.getList().getBoard().getId())) {
                throw new IllegalArgumentException("Cannot move card to a list in a different board");
            }
            
            card.setList(newList);
        }
        
        // Update assigned user if changed
        if (dto.getAssignedToId() != null) {
            if (card.getAssignedTo() == null || !dto.getAssignedToId().equals(card.getAssignedTo().getId())) {
                UserEntity assignedTo = userRepository
                        .findById(dto.getAssignedToId()).get();
                card.setAssignedTo(assignedTo);
            }
        } else if (card.getAssignedTo() != null) {
            card.setAssignedTo(null);
        }
        
        cardMapper.updateEntityFromDto(dto, card);
        card = cardRepository.save(card);
        
        CardWithItsListRead result = cardMapper.toDtoWithItsList(card);

        eventPublisher.publishEvent(new CardUpdatedEvent(
                card.getId(),
                card.getList().getId(),
                card.getList().getBoard().getId(),
                result
        ));
        
        return result;
    }

    @Transactional
    public void updateCardPositions(UserEntity user, Long listId, List<Long> cardIds) 
            throws ListNotFoundException, InsufficientBoardPermissionsException {
        ListEntity list = listRepository.findById(listId)
            .orElseThrow(() -> new ListNotFoundException("List not found with id: " + listId));
        
        Long boardId = list.getBoard().getId();
        
        if (!boardPermissionService.hasPermission(boardId, user.getId(), "EDIT")) {
            throw new InsufficientBoardPermissionsException("User does not have permission to update card positions");
        }
        
        // Оновлюємо позиції карток
        for (int i = 0; i < cardIds.size(); i++) {
            Long cardId = cardIds.get(i);
            CardEntity card = cardRepository.findById(cardId).orElse(null);
            
            if (card != null && card.getList().getId().equals(listId)) {
                card.setPosition(i);
                cardRepository.save(card);
            }
        }

        eventPublisher.publishEvent(new CardPositionsUpdatedEvent(
                listId,
                boardId,
                cardIds
        ));
    }
    
    @Transactional
    public CardWithItsListRead deleteCard(UserEntity user, Long id)
            throws CardNotFoundException, InsufficientCardPermissionsException {
        CardEntity card = cardRepository.findById(id)
            .orElseThrow(() -> new CardNotFoundException("Task not found with id: " + id));
        
        if (!boardPermissionService.hasPermission(card.getList().getBoard().getId(), user.getId(), "EDIT")) {
            throw new InsufficientCardPermissionsException("User does not have permission to delete this task");
        }

        Long boardId = card.getList().getBoard().getId();
        Long listId = card.getList().getId();
        CardWithItsListRead result = cardMapper.toDtoWithItsList(card);

        cardRepository.deleteCommentsByCardId(id);
        cardRepository.deleteTasksByCardId(id);
        cardRepository.deleteById(card.getId());

        eventPublisher.publishEvent(new CardDeletedEvent(
                card.getId(),
                listId,
                boardId,
                result
        ));
        
        return result;
    }
}
