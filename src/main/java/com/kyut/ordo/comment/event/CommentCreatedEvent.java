package com.kyut.ordo.comment.event;

import com.kyut.ordo.comment.dto.CommentRead;
import com.kyut.ordo.core.common.event.DomainEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

/**
 * Подія створення коментаря
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CommentCreatedEvent extends DomainEvent {
    private final Long commentId;
    private final Long cardId;
    private final Long listId;
    private final Long boardId;
    private final CommentRead commentData;
    
    public CommentCreatedEvent(Long commentId, Long cardId, Long listId, Long boardId, CommentRead commentData) {
        super(UUID.randomUUID().toString(), "COMMENT_CREATED");
        this.commentId = commentId;
        this.cardId = cardId;
        this.listId = listId;
        this.boardId = boardId;
        this.commentData = commentData;
    }
}
