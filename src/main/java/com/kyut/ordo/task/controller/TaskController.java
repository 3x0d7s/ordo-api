package com.kyut.ordo.task.controller;

import java.util.List;

import com.kyut.ordo.task.dto.TaskRead;
import com.kyut.ordo.task.dto.TaskWithItsListRead;
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

import com.kyut.ordo.board.exception.InsufficientBoardPermissionsException;
import com.kyut.ordo.task.dto.TaskCreate;
import com.kyut.ordo.task.exception.InsufficientTaskPermissionsException;
import com.kyut.ordo.task.exception.TaskListNotFoundException;
import com.kyut.ordo.task.exception.TaskNotFoundException;
import com.kyut.ordo.task.service.TaskService;
import com.kyut.ordo.user.UserEntity;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;
    
    @GetMapping("/list/{listId}")
    public ResponseEntity<List<TaskRead>> findAllByList(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long listId) 
            throws TaskListNotFoundException, InsufficientBoardPermissionsException {
        List<TaskRead> tasks = taskService.findAllByList(user, listId);
        return ResponseEntity.ok(tasks);
    }
    
    @GetMapping("/list/{listId}/page")
    public ResponseEntity<Page<TaskRead>> findAllByListPaged(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long listId,
            Pageable pageable) 
            throws TaskListNotFoundException, InsufficientBoardPermissionsException {
        Page<TaskRead> tasks = taskService.findAllByList(user, listId, pageable);
        return ResponseEntity.ok(tasks);
    }
    
    @GetMapping("/assigned")
    public ResponseEntity<Page<TaskWithItsListRead>> findAllAssignedToUser(
            @AuthenticationPrincipal UserEntity user,
            Pageable pageable) {
        Page<TaskWithItsListRead> tasks = taskService.findAllAssignedToUser(user, pageable);
        return ResponseEntity.ok(tasks);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TaskWithItsListRead> findById(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long id) 
            throws TaskNotFoundException, InsufficientTaskPermissionsException {
        TaskWithItsListRead task = taskService.findById(user, id);
        return ResponseEntity.ok(task);
    }
    
    @PostMapping
    public ResponseEntity<TaskWithItsListRead> createTask(
            @AuthenticationPrincipal UserEntity user,
            @RequestBody TaskCreate dto) 
            throws TaskListNotFoundException, InsufficientTaskPermissionsException {
        TaskWithItsListRead task = taskService.createTask(user, dto);
        return ResponseEntity.ok(task);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TaskWithItsListRead> updateTask(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long id,
            @RequestBody TaskCreate dto) 
            throws TaskNotFoundException, InsufficientTaskPermissionsException {
        TaskWithItsListRead task = taskService.updateTask(user, id, dto);
        return ResponseEntity.ok(task);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<TaskWithItsListRead> deleteTask(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long id) 
            throws TaskNotFoundException, InsufficientTaskPermissionsException {
        TaskWithItsListRead task = taskService.deleteTask(user, id);
        return ResponseEntity.ok(task);
    }
}
