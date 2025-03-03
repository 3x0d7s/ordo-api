package com.kyut.ordo.task.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kyut.ordo.board.entity.BoardEntity;
import com.kyut.ordo.board.exception.BoardNotFoundException;
import com.kyut.ordo.board.exception.InsufficientBoardPermissionsException;
import com.kyut.ordo.board.repository.BoardRepository;
import com.kyut.ordo.board.service.BoardPermissionService;
import com.kyut.ordo.task.dto.TaskListCreate;
import com.kyut.ordo.task.dto.TaskListRead;
import com.kyut.ordo.task.entity.TaskListEntity;
import com.kyut.ordo.task.exception.TaskListNotFoundException;
import com.kyut.ordo.task.mapper.TaskListMapper;
import com.kyut.ordo.task.repository.TaskListRepository;
import com.kyut.ordo.user.UserEntity;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskListService {
    private final TaskListRepository taskListRepository;
    private final BoardRepository boardRepository;
    private final BoardPermissionService boardPermissionService;
    private final TaskListMapper taskListMapper;
    
    @Transactional(readOnly = true)
    public List<TaskListRead> findAllByBoard(UserEntity user, Long boardId) 
            throws BoardNotFoundException, InsufficientBoardPermissionsException {
        BoardEntity board = boardRepository.findById(boardId)
            .orElseThrow(() -> new BoardNotFoundException("Board not found with id: " + boardId));
        
        if (!boardPermissionService.hasPermission(boardId, user.getId(), "EDIT")) {
            throw new InsufficientBoardPermissionsException("User does not have permission to view lists in this board");
        }
        
        List<TaskListEntity> lists = taskListRepository.findAllByBoardOrderByPosition(board);
        return lists.stream()
            .map(taskListMapper::toDto)
            .toList();
    }
    
    @Transactional(readOnly = true)
    public Page<TaskListRead> findAllByBoard(UserEntity user, Long boardId, Pageable pageable) 
            throws BoardNotFoundException, InsufficientBoardPermissionsException {
        BoardEntity board = boardRepository.findById(boardId)
            .orElseThrow(() -> new BoardNotFoundException("Board not found with id: " + boardId));
        
        if (!boardPermissionService.hasPermission(boardId, user.getId(), "EDIT")) {
            throw new InsufficientBoardPermissionsException("User does not have permission to view lists in this board");
        }
        
        return taskListRepository.findAllByBoard(board, pageable)
            .map(taskListMapper::toDto);
    }
    
    @Transactional(readOnly = true)
    public TaskListRead findById(UserEntity user, Long id) 
            throws TaskListNotFoundException, InsufficientBoardPermissionsException {
        TaskListEntity taskList = taskListRepository.findById(id)
            .orElseThrow(() -> new TaskListNotFoundException("Task list not found with id: " + id));
        
        if (!boardPermissionService.hasPermission(taskList.getBoard().getId(), user.getId(), "EDIT")) {
            throw new InsufficientBoardPermissionsException("User does not have permission to view this list");
        }
        
        return taskListMapper.toDto(taskList);
    }
    
    @Transactional
    public TaskListRead createTaskList(UserEntity user, TaskListCreate dto) 
            throws BoardNotFoundException, InsufficientBoardPermissionsException {
        BoardEntity board = boardRepository.findById(dto.getBoardId())
            .orElseThrow(() -> new BoardNotFoundException("Board not found with id: " + dto.getBoardId()));
        
        if (!boardPermissionService.hasPermission(dto.getBoardId(), user.getId(), "CREATE_LISTS")) {
            throw new InsufficientBoardPermissionsException("User does not have permission to create lists in this board");
        }
        
        // If position is not provided, add to the end
        if (dto.getPosition() == null) {
            Integer listCount = taskListRepository.countByBoard(board);
            dto.setPosition(listCount);
        }
        
        TaskListEntity taskList = taskListMapper.toEntity(dto, board);
        taskList.setCreatedAt(LocalDateTime.now());
        taskList = taskListRepository.save(taskList);
        
        return taskListMapper.toDto(taskList);
    }
    
    @Transactional
    public TaskListRead updateTaskList(UserEntity user, Long id, TaskListCreate dto) 
            throws TaskListNotFoundException, InsufficientBoardPermissionsException {
        TaskListEntity taskList = taskListRepository.findById(id)
            .orElseThrow(() -> new TaskListNotFoundException("Task list not found with id: " + id));
        
        if (!boardPermissionService.hasPermission(taskList.getBoard().getId(), user.getId(), "EDIT")) {
            throw new InsufficientBoardPermissionsException("User does not have permission to edit this list");
        }
        
        // Don't allow changing the board
        if (dto.getBoardId() != null && !dto.getBoardId().equals(taskList.getBoard().getId())) {
            throw new IllegalArgumentException("Cannot change the board of a task list");
        }
        
        taskListMapper.updateEntityFromDto(dto, taskList);
        taskList = taskListRepository.save(taskList);
        
        return taskListMapper.toDto(taskList);
    }
    
    @Transactional
    public TaskListRead deleteTaskList(UserEntity user, Long id) 
            throws TaskListNotFoundException, InsufficientBoardPermissionsException {
        TaskListEntity taskList = taskListRepository.findById(id)
            .orElseThrow(() -> new TaskListNotFoundException("Task list not found with id: " + id));
        
        if (!boardPermissionService.hasPermission(taskList.getBoard().getId(), user.getId(), "EDIT")) {
            throw new InsufficientBoardPermissionsException("User does not have permission to delete this list");
        }
        
        taskListRepository.delete(taskList);
        
        return taskListMapper.toDto(taskList);
    }
}
