package com.kyut.ordo.feature.list.service;

import java.util.List;

import com.kyut.ordo.feature.list.event.ListCreatedEvent;
import com.kyut.ordo.feature.list.event.ListDeletedEvent;
import com.kyut.ordo.feature.list.event.ListPositionsUpdatedEvent;
import com.kyut.ordo.feature.list.event.ListUpdatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kyut.ordo.feature.list.dto.ListRead;
import com.kyut.ordo.feature.list.entity.ListEntity;
import com.kyut.ordo.feature.board.entity.BoardEntity;
import com.kyut.ordo.feature.board.exception.BoardNotFoundException;
import com.kyut.ordo.feature.board.exception.InsufficientBoardPermissionsException;
import com.kyut.ordo.feature.board.repository.BoardRepository;
import com.kyut.ordo.feature.board.service.BoardPermissionService;
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
    private final BoardPermissionService boardPermissionService;
    private final ListMapper listMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public List<ListRead> findAllByBoard(UserEntity user, Long boardId)
            throws BoardNotFoundException, InsufficientBoardPermissionsException {
        BoardEntity board = boardRepository.findById(boardId)
            .orElseThrow(() -> new BoardNotFoundException("Board not found with id: " + boardId));
        
//        if (!boardPermissionService.hasPermission(boardId, user.getId(), "EDIT")) {
//            throw new InsufficientBoardPermissionsException("User does not have permission to view lists in this board");
//        }
        
        List<ListEntity> lists = listRepository.findAllByBoardOrderByPosition(board);
        return lists.stream()
            .map(listMapper::toDto)
            .toList();
    }
    
    @Transactional(readOnly = true)
    public Page<ListRead> findAllByBoard(UserEntity user, Long boardId, Pageable pageable)
            throws BoardNotFoundException, InsufficientBoardPermissionsException {
        BoardEntity board = boardRepository.findById(boardId)
            .orElseThrow(() -> new BoardNotFoundException("Board not found with id: " + boardId));
        
//        if (!boardPermissionService.hasPermission(boardId, user.getId(), "EDIT")) {
//            throw new InsufficientBoardPermissionsException("User does not have permission to view lists in this board");
//        }
        
        return listRepository.findAllByBoard(board, pageable)
            .map(listMapper::toDto);
    }
    
    @Transactional(readOnly = true)
    public ListRead findById(UserEntity user, Long id)
            throws ListNotFoundException, InsufficientBoardPermissionsException {
        ListEntity taskList = listRepository.findById(id)
            .orElseThrow(() -> new ListNotFoundException("Task list not found with id: " + id));
        
        if (!boardPermissionService.hasPermission(taskList.getBoard().getId(), user.getId(), "EDIT")) {
            throw new InsufficientBoardPermissionsException("User does not have permission to view this list");
        }
        
        return listMapper.toDto(taskList);
    }
    
    @Transactional
    public ListRead createTaskList(UserEntity user, ListCreate dto)
            throws BoardNotFoundException, InsufficientBoardPermissionsException {
        BoardEntity board = boardRepository.findById(dto.getBoardId())
            .orElseThrow(() -> new BoardNotFoundException("Board not found with id: " + dto.getBoardId()));
        
        if (!boardPermissionService.hasPermission(dto.getBoardId(), user.getId(), "CREATE_LISTS")) {
            throw new InsufficientBoardPermissionsException("User does not have permission to create lists in this board");
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
    public ListRead updateTaskList(UserEntity user, Long id, ListCreate dto)
            throws ListNotFoundException, InsufficientBoardPermissionsException {
        ListEntity taskList = listRepository.findById(id)
            .orElseThrow(() -> new ListNotFoundException("Task list not found with id: " + id));
        
        if (!boardPermissionService.hasPermission(taskList.getBoard().getId(), user.getId(), "EDIT")) {
            throw new InsufficientBoardPermissionsException("User does not have permission to edit this list");
        }
        
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
    public void updateListPositions(UserEntity user, Long boardId, List<Long> listIds) 
            throws BoardNotFoundException, InsufficientBoardPermissionsException {
        BoardEntity board = boardRepository.findById(boardId)
            .orElseThrow(() -> new BoardNotFoundException("Board not found with id: " + boardId));
        
        if (!boardPermissionService.hasPermission(boardId, user.getId(), "EDIT")) {
            throw new InsufficientBoardPermissionsException("User does not have permission to update list positions");
        }
        
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
    public ListRead deleteList(UserEntity user, Long id)
            throws ListNotFoundException, InsufficientBoardPermissionsException {
        ListEntity list = listRepository.findById(id)
            .orElseThrow(() -> new ListNotFoundException("Task list not found with id: " + id));
        
        Long boardId = list.getBoard().getId();
        if (!boardPermissionService.hasPermission(boardId, user.getId(), "EDIT")) {
            throw new InsufficientBoardPermissionsException("User does not have permission to delete this list");
        }

        listRepository.deleteTasksByCardsOfList(id);
        listRepository.deleteCommentsByCardsOfList(id);
        
        listRepository.deleteCardsByListId(id);
        listRepository.deleteListById(id);

        ListRead listRead = listMapper.toDto(list);

        // Публікуємо подію видалення списку
        eventPublisher.publishEvent(new ListDeletedEvent(
            id,
            boardId,
            listRead
        ));

        return listRead;
    }
}
