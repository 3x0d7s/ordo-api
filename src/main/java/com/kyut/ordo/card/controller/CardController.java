package com.kyut.ordo.card.controller;

import com.kyut.ordo.board.exception.InsufficientBoardPermissionsException;
import com.kyut.ordo.card.dto.CardCreate;
import com.kyut.ordo.card.dto.CardWithItsListRead;
import com.kyut.ordo.comment.dto.CommentRead;
import com.kyut.ordo.comment.service.CommentService;
import com.kyut.ordo.task.dto.TaskRead;
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

import com.kyut.ordo.card.exception.InsufficientCardPermissionsException;
import com.kyut.ordo.list.exception.ListNotFoundException;
import com.kyut.ordo.card.exception.CardNotFoundException;
import com.kyut.ordo.card.service.CardService;
import com.kyut.ordo.user.entity.UserEntity;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
public class CardController {
    private final CardService cardService;
    private final TaskService taskService;
    private final CommentService commentService;
    
    @GetMapping("/assigned")
    public ResponseEntity<Page<CardWithItsListRead>> findAllAssignedToUser(
            @AuthenticationPrincipal UserEntity user,
            Pageable pageable) {
        Page<CardWithItsListRead> tasks = cardService.findAllAssignedToUser(user, pageable);
        return ResponseEntity.ok(tasks);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CardWithItsListRead> findById(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long id) 
            throws CardNotFoundException, InsufficientCardPermissionsException {
        CardWithItsListRead task = cardService.findById(user, id);
        return ResponseEntity.ok(task);
    }
    
    @PostMapping
    public ResponseEntity<CardWithItsListRead> createTask(
            @AuthenticationPrincipal UserEntity user,
            @RequestBody CardCreate dto)
            throws ListNotFoundException, InsufficientCardPermissionsException {
        CardWithItsListRead task = cardService.createCard(user, dto);
        return ResponseEntity.ok(task);
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<Page<CommentRead>> findAllCommentsByCard(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long id,
            Pageable pageable)
            throws CardNotFoundException, InsufficientBoardPermissionsException {
        Page<CommentRead> comments = commentService.findAllByCard(user, id, pageable);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/{id}/tasks")
    public ResponseEntity<Page<TaskRead>> findAllByCard(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long id,
            Pageable pageable)
            throws CardNotFoundException, InsufficientBoardPermissionsException {
        Page<TaskRead> tasks = taskService.findAllByCard(user, id, pageable);
        return ResponseEntity.ok(tasks);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<CardWithItsListRead> updateTask(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long id,
            @RequestBody CardCreate dto)
            throws CardNotFoundException, InsufficientCardPermissionsException {
        CardWithItsListRead task = cardService.updateCard(user, id, dto);
        return ResponseEntity.ok(task);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<CardWithItsListRead> deleteTask(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long id) 
            throws CardNotFoundException, InsufficientCardPermissionsException {
        CardWithItsListRead task = cardService.deleteCard(user, id);
        return ResponseEntity.ok(task);
    }
}
