package com.kyut.ordo.comment.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kyut.ordo.board.exception.InsufficientBoardPermissionsException;
import com.kyut.ordo.board.service.BoardPermissionService;
import com.kyut.ordo.comment.dto.CommentCreate;
import com.kyut.ordo.comment.dto.CommentRead;
import com.kyut.ordo.comment.entity.CommentEntity;
import com.kyut.ordo.comment.exception.CommentNotFoundException;
import com.kyut.ordo.comment.exception.InsufficientCommentPermissionsException;
import com.kyut.ordo.comment.mapper.CommentMapper;
import com.kyut.ordo.comment.repository.CommentRepository;
import com.kyut.ordo.card.entity.CardEntity;
import com.kyut.ordo.card.exception.CardNotFoundException;
import com.kyut.ordo.card.repository.CardRepository;
import com.kyut.ordo.user.UserEntity;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final CardRepository cardRepository;
    private final BoardPermissionService boardPermissionService;
    private final CommentMapper commentMapper;
    
    @Transactional(readOnly = true)
    public List<CommentRead> findAllByCard(UserEntity user, Long cardId)
            throws CardNotFoundException, InsufficientBoardPermissionsException {
        CardEntity card = cardRepository.findById(cardId)
            .orElseThrow(() -> new CardNotFoundException("Task not found with id: " + cardId));
        
        if (!boardPermissionService.hasPermission(card.getTaskList().getBoard().getId(), user.getId(), "EDIT")) {
            throw new InsufficientBoardPermissionsException("User does not have permission to view comments in this task");
        }

        List<CommentEntity> comments = commentRepository.findAllByCardOrderByCreatedAtDesc(card);
        return comments.stream()
            .map(commentMapper::toDto)
            .toList();
    }
    
    @Transactional(readOnly = true)
    public Page<CommentRead> findAllByCard(UserEntity user, Long cardId, Pageable pageable)
            throws CardNotFoundException, InsufficientBoardPermissionsException {
        CardEntity card = cardRepository.findById(cardId)
            .orElseThrow(() -> new CardNotFoundException("Task not found with id: " + cardId));
        
        if (!boardPermissionService.hasPermission(card.getTaskList().getBoard().getId(), user.getId(), "EDIT")) {
            throw new InsufficientBoardPermissionsException("User does not have permission to view comments in this task");
        }
        
        return commentRepository.findAllByCard(card, pageable)
            .map(commentMapper::toDto);
    }
    
    @Transactional(readOnly = true)
    public Page<CommentRead> findAllByUser(UserEntity user, Pageable pageable) {
        return commentRepository.findAllByCreatedBy(user, pageable)
            .map(commentMapper::toDto);
    }
    
    @Transactional(readOnly = true)
    public CommentRead findById(UserEntity user, Long id) 
            throws CommentNotFoundException, InsufficientCommentPermissionsException {
        CommentEntity comment = commentRepository.findById(id)
            .orElseThrow(() -> new CommentNotFoundException("Comment not found with id: " + id));
        
        if (!boardPermissionService.hasPermission(comment.getCard().getTaskList().getBoard().getId(), user.getId(), "EDIT")) {
            throw new InsufficientCommentPermissionsException("User does not have permission to view this comment");
        }
        
        return commentMapper.toDto(comment);
    }
    
    @Transactional
    public CommentRead createComment(UserEntity user, CommentCreate dto) 
            throws CardNotFoundException, InsufficientCommentPermissionsException {
        CardEntity card = cardRepository.findById(dto.getCardId())
            .orElseThrow(() -> new CardNotFoundException("Task not found with id: " + dto.getCardId()));
        
        if (!boardPermissionService.hasPermission(card.getTaskList().getBoard().getId(), user.getId(), "CREATE_TASKS")) {
            throw new InsufficientCommentPermissionsException("User does not have permission to create comments in this task");
        }
        
        CommentEntity comment = commentMapper.toEntity(dto, user, card);
        comment.setCreatedAt(LocalDateTime.now());
        comment = commentRepository.save(comment);
        
        return commentMapper.toDto(comment);
    }
    
    @Transactional
    public CommentRead updateComment(UserEntity user, Long id, CommentCreate dto) 
            throws CommentNotFoundException, InsufficientCommentPermissionsException {
        CommentEntity comment = commentRepository.findById(id)
            .orElseThrow(() -> new CommentNotFoundException("Comment not found with id: " + id));
        
        // Only the creator of the comment or a board admin can update it
        if (!comment.getCreatedBy().getId().equals(user.getId()) &&
            !boardPermissionService.hasPermission(comment.getCard().getTaskList().getBoard().getId(), user.getId(), "MANAGE_ROLES")) {
            throw new InsufficientCommentPermissionsException("User does not have permission to edit this comment");
        }
        
        // Don't allow changing the card or created by
        if (dto.getCardId() != null && !dto.getCardId().equals(comment.getCard().getId())) {
            throw new IllegalArgumentException("Cannot change the task of a comment");
        }
        
        // Only update the message
        comment.setMessage(dto.getMessage());
        comment = commentRepository.save(comment);
        
        return commentMapper.toDto(comment);
    }
    
    @Transactional
    public CommentRead deleteComment(UserEntity user, Long id) 
            throws CommentNotFoundException, InsufficientCommentPermissionsException {
        CommentEntity comment = commentRepository.findById(id)
            .orElseThrow(() -> new CommentNotFoundException("Comment not found with id: " + id));
        
        // Only the creator of the comment or a board admin can delete it
        if (!comment.getCreatedBy().getId().equals(user.getId()) && 
            !boardPermissionService.hasPermission(comment.getCard().getTaskList().getBoard().getId(), user.getId(), "MANAGE_ROLES")) {
            throw new InsufficientCommentPermissionsException("User does not have permission to delete this comment");
        }
        
        commentRepository.delete(comment);
        
        return commentMapper.toDto(comment);
    }
}
