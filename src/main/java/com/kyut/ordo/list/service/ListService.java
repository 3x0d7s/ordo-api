package com.kyut.ordo.list.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kyut.ordo.common.dto.WebSocketMessage;
import com.kyut.ordo.common.service.WebSocketService;
import com.kyut.ordo.list.dto.ListRead;
import com.kyut.ordo.list.entity.ListEntity;
import com.kyut.ordo.board.entity.BoardEntity;
import com.kyut.ordo.board.exception.BoardNotFoundException;
import com.kyut.ordo.board.exception.InsufficientBoardPermissionsException;
import com.kyut.ordo.board.repository.BoardRepository;
import com.kyut.ordo.board.service.BoardPermissionService;
import com.kyut.ordo.list.dto.ListCreate;
import com.kyut.ordo.list.exception.ListNotFoundException;
import com.kyut.ordo.list.mapper.ListMapper;
import com.kyut.ordo.list.repository.ListRepository;
import com.kyut.ordo.user.entity.UserEntity;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListService {
    private final ListRepository listRepository;
    private final BoardRepository boardRepository;
    private final BoardPermissionService boardPermissionService;
    private final ListMapper listMapper;
    private final WebSocketService webSocketService;

    @Transactional(readOnly = true)
    public List<ListRead> findAllByBoard(UserEntity user, Long boardId)
            throws BoardNotFoundException, InsufficientBoardPermissionsException {
        BoardEntity board = boardRepository.findById(boardId)
            .orElseThrow(() -> new BoardNotFoundException("Board not found with id: " + boardId));
        
        if (!boardPermissionService.hasPermission(boardId, user.getId(), "EDIT")) {
            throw new InsufficientBoardPermissionsException("User does not have permission to view lists in this board");
        }
        
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
        
        if (!boardPermissionService.hasPermission(boardId, user.getId(), "EDIT")) {
            throw new InsufficientBoardPermissionsException("User does not have permission to view lists in this board");
        }
        
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
//        taskList.setCreatedAt(LocalDateTime.now());
        taskList = listRepository.save(taskList);

        ListRead result = listMapper.toDto(taskList);

        WebSocketMessage<ListRead> message = WebSocketMessage.<ListRead>builder()
                .type(WebSocketMessage.WebSocketMessageType.LIST_CREATED)
                .payload(result)
                .entityId(taskList.getId().toString())
                .build();

        webSocketService.sendBoardMessage(taskList.getBoard().getId(), message);

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

        WebSocketMessage<ListRead> message = WebSocketMessage.<ListRead>builder()
                .type(WebSocketMessage.WebSocketMessageType.LIST_UPDATED)
                .payload(result)
                .entityId(id.toString())
                .build();

        webSocketService.sendBoardMessage(result.getBoard().getId(), message);
        webSocketService.sendListMessage(id, message);

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
        
        // Оновлюємо позиції списків
        for (int i = 0; i < listIds.size(); i++) {
            Long listId = listIds.get(i);
            ListEntity list = listRepository.findById(listId).orElse(null);
            
            if (list != null && list.getBoard().getId().equals(boardId)) {
                list.setPosition(i);
                listRepository.save(list);
            }
        }
        
        // Надсилаємо повідомлення про оновлення позицій
        WebSocketMessage<List<Long>> message = WebSocketMessage.<List<Long>>builder()
                .type(WebSocketMessage.WebSocketMessageType.LIST_POSITIONS_UPDATED)
                .payload(listIds)
                .entityId(boardId.toString())
                .build();
                
        webSocketService.sendBoardMessage(boardId, message);
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

        WebSocketMessage<ListRead> message = WebSocketMessage.<ListRead>builder()
                .type(WebSocketMessage.WebSocketMessageType.LIST_DELETED)
                .payload(listRead)
                .entityId(listRead.getId().toString())
                .build();

        webSocketService.sendBoardMessage(listRead.getBoard().getId(), message);
        webSocketService.sendListMessage(id, message);

        return listRead;
    }
}
