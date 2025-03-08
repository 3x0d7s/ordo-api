package com.kyut.ordo.comment.controller;

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

import com.kyut.ordo.board.exception.InsufficientBoardPermissionsException;
import com.kyut.ordo.comment.dto.CommentCreate;
import com.kyut.ordo.comment.dto.CommentRead;
import com.kyut.ordo.comment.exception.CommentNotFoundException;
import com.kyut.ordo.comment.exception.InsufficientCommentPermissionsException;
import com.kyut.ordo.comment.service.CommentService;
import com.kyut.ordo.task.exception.TaskNotFoundException;
import com.kyut.ordo.user.UserEntity;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    
    @GetMapping("/card/{cardId}")
    public ResponseEntity<List<CommentRead>> findAllByCard(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long cardId) 
            throws TaskNotFoundException, InsufficientBoardPermissionsException {
        List<CommentRead> comments = commentService.findAllByTask(user, cardId);
        return ResponseEntity.ok(comments);
    }
    
    @GetMapping("/card/{cardId}/page")
    public ResponseEntity<Page<CommentRead>> findAllByCardPaged(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long cardId,
            Pageable pageable) 
            throws TaskNotFoundException, InsufficientBoardPermissionsException {
        Page<CommentRead> comments = commentService.findAllByTask(user, cardId, pageable);
        return ResponseEntity.ok(comments);
    }
    
    @GetMapping("/user")
    public ResponseEntity<Page<CommentRead>> findAllByUser(
            @AuthenticationPrincipal UserEntity user,
            Pageable pageable) {
        Page<CommentRead> comments = commentService.findAllByUser(user, pageable);
        return ResponseEntity.ok(comments);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CommentRead> findById(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long id) 
            throws CommentNotFoundException, InsufficientCommentPermissionsException {
        CommentRead comment = commentService.findById(user, id);
        return ResponseEntity.ok(comment);
    }
    
    @PostMapping
    public ResponseEntity<CommentRead> createComment(
            @AuthenticationPrincipal UserEntity user,
            @RequestBody CommentCreate dto) 
            throws TaskNotFoundException, InsufficientCommentPermissionsException {
        CommentRead comment = commentService.createComment(user, dto);
        return ResponseEntity.ok(comment);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<CommentRead> updateComment(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long id,
            @RequestBody CommentCreate dto) 
            throws CommentNotFoundException, InsufficientCommentPermissionsException {
        CommentRead comment = commentService.updateComment(user, id, dto);
        return ResponseEntity.ok(comment);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<CommentRead> deleteComment(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long id) 
            throws CommentNotFoundException, InsufficientCommentPermissionsException {
        CommentRead comment = commentService.deleteComment(user, id);
        return ResponseEntity.ok(comment);
    }
}
