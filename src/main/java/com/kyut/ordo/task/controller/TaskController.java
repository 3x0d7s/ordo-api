package com.kyut.ordo.task.controller;

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
import com.kyut.ordo.card.exception.CardNotFoundException;
import com.kyut.ordo.task.dto.TaskCreate;
import com.kyut.ordo.task.dto.TaskRead;
import com.kyut.ordo.task.exception.InsufficientTaskPermissionsException;
import com.kyut.ordo.task.exception.TaskNotFoundException;
import com.kyut.ordo.task.service.TaskService;
import com.kyut.ordo.user.UserEntity;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;
    
    @GetMapping("/{id}")
    public ResponseEntity<TaskRead> findById(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long id) 
            throws TaskNotFoundException, InsufficientTaskPermissionsException {
        TaskRead task = taskService.findById(user, id);
        return ResponseEntity.ok(task);
    }
    
    @PostMapping
    public ResponseEntity<TaskRead> createTask(
            @AuthenticationPrincipal UserEntity user,
            @RequestBody TaskCreate dto)
            throws CardNotFoundException, InsufficientTaskPermissionsException {
        TaskRead task = taskService.createTask(user, dto);
        return ResponseEntity.ok(task);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TaskRead> updateTask(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long id,
            @RequestBody TaskCreate dto)
            throws TaskNotFoundException, InsufficientTaskPermissionsException, CardNotFoundException {
        TaskRead task = taskService.updateTask(user, id, dto);
        return ResponseEntity.ok(task);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<TaskRead> deleteTask(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long id) 
            throws TaskNotFoundException, InsufficientTaskPermissionsException {
        TaskRead task = taskService.deleteTask(user, id);
        return ResponseEntity.ok(task);
    }
}
