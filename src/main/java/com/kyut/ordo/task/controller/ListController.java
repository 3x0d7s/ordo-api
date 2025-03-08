package com.kyut.ordo.task.controller;

import com.kyut.ordo.task.dto.CardRead;
import com.kyut.ordo.task.dto.ListRead;
import com.kyut.ordo.task.service.TaskService;
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
import com.kyut.ordo.task.dto.ListCreate;
import com.kyut.ordo.task.exception.TaskListNotFoundException;
import com.kyut.ordo.task.service.TaskListService;
import com.kyut.ordo.user.UserEntity;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/lists")
@RequiredArgsConstructor
public class ListController {
    private final TaskListService taskListService;
    private final TaskService taskService;


//    @GetMapping("/board/{boardId}")
//    public ResponseEntity<List<TaskListRead>> findAllByBoard(
//            @AuthenticationPrincipal UserEntity user,
//            @PathVariable Long boardId)
//            throws BoardNotFoundException, InsufficientBoardPermissionsException {
//        List<TaskListRead> taskLists = taskListService.findAllByBoard(user, boardId);
//        return ResponseEntity.ok(taskLists);
//    }
//
//    @GetMapping("/board/{boardId}/page")
//    public ResponseEntity<Page<TaskListRead>> findAllByBoardPaged(
//            @AuthenticationPrincipal UserEntity user,
//            @PathVariable Long boardId,
//            Pageable pageable)
//            throws BoardNotFoundException, InsufficientBoardPermissionsException {
//        Page<TaskListRead> taskLists = taskListService.findAllByBoard(user, boardId, pageable);
//        return ResponseEntity.ok(taskLists);
//    }

    @GetMapping("/{id}")
    public ResponseEntity<ListRead> findById(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long id) 
            throws TaskListNotFoundException, InsufficientBoardPermissionsException {
        ListRead taskList = taskListService.findById(user, id);
        return ResponseEntity.ok(taskList);
    }

    @GetMapping("/{id}/tasks")
    public ResponseEntity<Page<CardRead>> findAllByListPaged(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long id,
            Pageable pageable)
            throws TaskListNotFoundException, InsufficientBoardPermissionsException {
        Page<CardRead> tasks = taskService.findAllByTaskList(user, id, pageable);
        return ResponseEntity.ok(tasks);
    }
    
    @PostMapping
    public ResponseEntity<ListRead> createTaskList(
            @AuthenticationPrincipal UserEntity user,
            @RequestBody ListCreate dto)
            throws BoardNotFoundException, InsufficientBoardPermissionsException {
        ListRead taskList = taskListService.createTaskList(user, dto);
        return ResponseEntity.ok(taskList);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ListRead> updateTaskList(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long id,
            @RequestBody ListCreate dto)
            throws TaskListNotFoundException, InsufficientBoardPermissionsException {
        ListRead taskList = taskListService.updateTaskList(user, id, dto);
        return ResponseEntity.ok(taskList);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ListRead> deleteTaskList(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long id) 
            throws TaskListNotFoundException, InsufficientBoardPermissionsException {
        ListRead taskList = taskListService.deleteTaskList(user, id);
        return ResponseEntity.ok(taskList);
    }
}
