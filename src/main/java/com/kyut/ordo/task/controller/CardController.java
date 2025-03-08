package com.kyut.ordo.task.controller;

import com.kyut.ordo.task.dto.CardCreate;
import com.kyut.ordo.task.dto.CardWithItsListRead;
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

import com.kyut.ordo.task.exception.InsufficientTaskPermissionsException;
import com.kyut.ordo.task.exception.TaskListNotFoundException;
import com.kyut.ordo.task.exception.TaskNotFoundException;
import com.kyut.ordo.task.service.TaskService;
import com.kyut.ordo.user.UserEntity;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
public class CardController {
    private final TaskService taskService;
    
    @GetMapping("/assigned")
    public ResponseEntity<Page<CardWithItsListRead>> findAllAssignedToUser(
            @AuthenticationPrincipal UserEntity user,
            Pageable pageable) {
        Page<CardWithItsListRead> tasks = taskService.findAllAssignedToUser(user, pageable);
        return ResponseEntity.ok(tasks);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CardWithItsListRead> findById(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long id) 
            throws TaskNotFoundException, InsufficientTaskPermissionsException {
        CardWithItsListRead task = taskService.findById(user, id);
        return ResponseEntity.ok(task);
    }
    
    @PostMapping
    public ResponseEntity<CardWithItsListRead> createTask(
            @AuthenticationPrincipal UserEntity user,
            @RequestBody CardCreate dto)
            throws TaskListNotFoundException, InsufficientTaskPermissionsException {
        CardWithItsListRead task = taskService.createTask(user, dto);
        return ResponseEntity.ok(task);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<CardWithItsListRead> updateTask(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long id,
            @RequestBody CardCreate dto)
            throws TaskNotFoundException, InsufficientTaskPermissionsException {
        CardWithItsListRead task = taskService.updateTask(user, id, dto);
        return ResponseEntity.ok(task);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<CardWithItsListRead> deleteTask(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long id) 
            throws TaskNotFoundException, InsufficientTaskPermissionsException {
        CardWithItsListRead task = taskService.deleteTask(user, id);
        return ResponseEntity.ok(task);
    }
}
