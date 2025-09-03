package com.kyut.ordo.feature.comment.event;

import com.kyut.ordo.feature.comment.dto.CommentRead;
import com.kyut.ordo.common.DomainEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

/**
 * Подія видалення коментаря
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CommentDeletedEvent extends DomainEvent {
    private final Long commentId;
    private final Long cardId;
    private final Long listId;
    private final Long boardId;
    private final CommentRead commentData;
    
    public CommentDeletedEvent(Long commentId, Long cardId, Long listId, Long boardId, CommentRead commentData) {
        super(UUID.randomUUID().toString(), "COMMENT_DELETED");
        this.commentId = commentId;
        this.cardId = cardId;
        this.listId = listId;
        this.boardId = boardId;
        this.commentData = commentData;
    }
}
