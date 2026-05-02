package com.kyut.ordo.feature.list.service;

import java.util.List;

import com.kyut.ordo.feature.list.event.ListCreatedEvent;
import com.kyut.ordo.feature.list.event.ListDeletedEvent;
import com.kyut.ordo.feature.list.event.ListPositionsUpdatedEvent;
import com.kyut.ordo.feature.list.event.ListUpdatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kyut.ordo.feature.list.dto.ListRead;
import com.kyut.ordo.feature.list.entity.ListEntity;
import com.kyut.ordo.feature.board.entity.BoardEntity;
import com.kyut.ordo.feature.board.exception.BoardNotFoundException;
import com.kyut.ordo.feature.board.exception.InsufficientBoardPermissionsException;
import com.kyut.ordo.feature.board.repository.BoardRepository;
import com.kyut.ordo.feature.list.dto.ListCreate;
import com.kyut.ordo.feature.list.exception.ListNotFoundException;
import com.kyut.ordo.feature.list.mapper.ListMapper;
import com.kyut.ordo.feature.list.repository.ListRepository;
import com.kyut.ordo.feature.user.entity.UserEntity;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListService {
    private final ListRepository listRepository;
    private final BoardRepository boardRepository;
    private final ListMapper listMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    @PreAuthorize("@featureAuthService.canAccessBoard(#p1, authentication)")
    public List<ListRead> findAllByBoard(UserEntity user, Long boardId)
            throws BoardNotFoundException, InsufficientBoardPermissionsException {
        BoardEntity board = boardRepository.findById(boardId)
            .orElseThrow(() -> new BoardNotFoundException("Board not found with id: " + boardId));

        List<ListEntity> lists = listRepository.findAllByBoardOrderByPosition(board);
        return lists.stream()
            .map(listMapper::toDto)
            .toList();
    }
    
    @Transactional(readOnly = true)
    @PreAuthorize("@featureAuthService.canAccessBoard(#p1, authentication)")
    public Page<ListRead> findAllByBoard(UserEntity user, Long boardId, Pageable pageable)
            throws BoardNotFoundException, InsufficientBoardPermissionsException {
        BoardEntity board = boardRepository.findById(boardId)
            .orElseThrow(() -> new BoardNotFoundException("Board not found with id: " + boardId));

        return listRepository.findAllByBoard(board, pageable)
            .map(listMapper::toDto);
    }
    
    @Transactional(readOnly = true)
    @PreAuthorize("@featureAuthService.canViewListCards(#p1, authentication)")
    public ListRead findById(UserEntity user, Long id)
            throws ListNotFoundException, InsufficientBoardPermissionsException {
        ListEntity taskList = listRepository.findById(id)
            .orElseThrow(() -> new ListNotFoundException("Task list not found with id: " + id));

        return listMapper.toDto(taskList);
    }
    
    @Transactional
    @PreAuthorize("@featureAuthService.canCreateLists(#p1.boardId, authentication)")
    public ListRead createTaskList(UserEntity user, ListCreate dto)
            throws BoardNotFoundException, InsufficientBoardPermissionsException {
        BoardEntity board = boardRepository.findById(dto.getBoardId())
            .orElseThrow(() -> new BoardNotFoundException("Board not found with id: " + dto.getBoardId()));

        if (dto.getTitle().isBlank()) {
            throw new IllegalArgumentException("Title cannot be blank");
        }
        
        // If position is not provided, add to the end
        if (dto.getPosition() == null) {
            Integer listCount = listRepository.countByBoard(board);
            dto.setPosition(listCount);
        }

        ListEntity taskList = listMapper.toEntity(dto, board);
        taskList = listRepository.save(taskList);

        ListRead result = listMapper.toDto(taskList);

        eventPublisher.publishEvent(new ListCreatedEvent(
            taskList.getId(),
            taskList.getBoard().getId(),
            result
        ));

        return result;
    }
    
    @Transactional
    @PreAuthorize("@featureAuthService.canViewListCards(#p1, authentication)")
    public ListRead updateTaskList(UserEntity user, Long id, ListCreate dto)
            throws ListNotFoundException, InsufficientBoardPermissionsException {
        ListEntity taskList = listRepository.findById(id)
            .orElseThrow(() -> new ListNotFoundException("Task list not found with id: " + id));

        // Don't allow changing the board
        if (dto.getBoardId() != null && !dto.getBoardId().equals(taskList.getBoard().getId())) {
            throw new IllegalArgumentException("Cannot change the board of a task list");
        }
        
        listMapper.updateEntityFromDto(dto, taskList);
        taskList = listRepository.save(taskList);

        ListRead result = listMapper.toDto(taskList);

        eventPublisher.publishEvent(new ListUpdatedEvent(
            id,
            result.getBoard().getId(),
            result
        ));

        return result;
    }
    
    @Transactional
    @PreAuthorize("@featureAuthService.canEditBoard(#p1, authentication)")
    public void updateListPositions(UserEntity user, Long boardId, List<Long> listIds) 
            throws BoardNotFoundException, InsufficientBoardPermissionsException {
        boardRepository.findById(boardId)
            .orElseThrow(() -> new BoardNotFoundException("Board not found with id: " + boardId));

        for (int i = 0; i < listIds.size(); i++) {
            Long listId = listIds.get(i);
            ListEntity list = listRepository.findById(listId).orElse(null);
            
            if (list != null && list.getBoard().getId().equals(boardId)) {
                list.setPosition(i);
                listRepository.save(list);
            }
        }
        
        eventPublisher.publishEvent(new ListPositionsUpdatedEvent(boardId, listIds));
    }
    
    @Transactional
    @PreAuthorize("@featureAuthService.canViewListCards(#p1, authentication)")
    public ListRead deleteList(UserEntity user, Long id)
            throws ListNotFoundException, InsufficientBoardPermissionsException {
        ListEntity list = listRepository.findById(id)
            .orElseThrow(() -> new ListNotFoundException("Task list not found with id: " + id));

        Long boardId = list.getBoard().getId();

        listRepository.deleteTasksByCardsOfList(id);
        listRepository.deleteCommentsByCardsOfList(id);
        
        listRepository.deleteCardsByListId(id);
        listRepository.delete(list);

        ListRead listRead = listMapper.toDto(list);

        eventPublisher.publishEvent(new ListDeletedEvent(
            id,
            boardId,
            listRead
        ));

        return listRead;
    }
}
