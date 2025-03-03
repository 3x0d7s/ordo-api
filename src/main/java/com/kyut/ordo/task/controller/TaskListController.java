package com.kyut.ordo.task.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kyut.ordo.board.exception.BoardNotFoundException;
import com.kyut.ordo.board.exception.InsufficientBoardPermissionsException;
import com.kyut.ordo.task.dto.TaskListCreate;
import com.kyut.ordo.task.dto.TaskListRead;
import com.kyut.ordo.task.exception.TaskListNotFoundException;
import com.kyut.ordo.task.service.TaskListService;
import com.kyut.ordo.user.UserEntity;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/tasklist")
@RequiredArgsConstructor
public class TaskListController {
    private final TaskListService taskListService;
    
    @GetMapping("/board/{boardId}")
    public ResponseEntity<List<TaskListRead>> findAllByBoard(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long boardId) 
            throws BoardNotFoundException, InsufficientBoardPermissionsException {
        List<TaskListRead> taskLists = taskListService.findAllByBoard(user, boardId);
        return ResponseEntity.ok(taskLists);
    }
    
    @GetMapping("/board/{boardId}/page")
    public ResponseEntity<Page<TaskListRead>> findAllByBoardPaged(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long boardId,
            Pageable pageable) 
            throws BoardNotFoundException, InsufficientBoardPermissionsException {
        Page<TaskListRead> taskLists = taskListService.findAllByBoard(user, boardId, pageable);
        return ResponseEntity.ok(taskLists);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TaskListRead> findById(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long id) 
            throws TaskListNotFoundException, InsufficientBoardPermissionsException {
        TaskListRead taskList = taskListService.findById(user, id);
        return ResponseEntity.ok(taskList);
    }
    
    @PostMapping
    public ResponseEntity<TaskListRead> createTaskList(
            @AuthenticationPrincipal UserEntity user,
            @RequestBody TaskListCreate dto) 
            throws BoardNotFoundException, InsufficientBoardPermissionsException {
        TaskListRead taskList = taskListService.createTaskList(user, dto);
        return ResponseEntity.ok(taskList);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TaskListRead> updateTaskList(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long id,
            @RequestBody TaskListCreate dto) 
            throws TaskListNotFoundException, InsufficientBoardPermissionsException {
        TaskListRead taskList = taskListService.updateTaskList(user, id, dto);
        return ResponseEntity.ok(taskList);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<TaskListRead> deleteTaskList(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long id) 
            throws TaskListNotFoundException, InsufficientBoardPermissionsException {
        TaskListRead taskList = taskListService.deleteTaskList(user, id);
        return ResponseEntity.ok(taskList);
    }
}
