package com.kyut.ordo.comment.listener;

import com.kyut.ordo.comment.dto.CommentRead;
import com.kyut.ordo.comment.event.CommentCreatedEvent;
import com.kyut.ordo.comment.event.CommentDeletedEvent;
import com.kyut.ordo.comment.event.CommentUpdatedEvent;
import com.kyut.ordo.core.websocket.dto.WebSocketMessage;
import com.kyut.ordo.core.websocket.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Event listener для обробки Comment подій та відправки WebSocket повідомлень
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CommentEventListener {
    
    private final WebSocketService webSocketService;
    
    /**
     * Обробляє подію створення коментаря
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentCreated(CommentCreatedEvent event) {
        log.debug("Processing COMMENT_CREATED event for commentId: {}, cardId: {}", 
                 event.getCommentId(), event.getCardId());
        
        try {
            WebSocketMessage<CommentRead> message = WebSocketMessage.<CommentRead>builder()
                .type(WebSocketMessage.WebSocketMessageType.COMMENT_CREATED)
                .payload(event.getCommentData())
                .entityId(event.getCommentId().toString())
                .build();
                
            webSocketService.sendBoardMessage(event.getBoardId(), message);
            webSocketService.sendListMessage(event.getListId(), message);
            webSocketService.sendCardMessage(event.getCardId(), message);
            
            log.info("Successfully sent COMMENT_CREATED WebSocket notification for commentId: {}", 
                    event.getCommentId());
            
        } catch (Exception e) {
            log.error("Failed to send COMMENT_CREATED WebSocket notification for commentId: {}", 
                     event.getCommentId(), e);
        }
    }
    
    /**
     * Обробляє подію оновлення коментаря
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentUpdated(CommentUpdatedEvent event) {
        log.debug("Processing COMMENT_UPDATED event for commentId: {}, cardId: {}", 
                 event.getCommentId(), event.getCardId());
        
        try {
            WebSocketMessage<CommentRead> message = WebSocketMessage.<CommentRead>builder()
                .type(WebSocketMessage.WebSocketMessageType.COMMENT_UPDATED)
                .payload(event.getCommentData())
                .entityId(event.getCommentId().toString())
                .build();
                
            webSocketService.sendBoardMessage(event.getBoardId(), message);
            webSocketService.sendListMessage(event.getListId(), message);
            webSocketService.sendCardMessage(event.getCardId(), message);
            
            log.info("Successfully sent COMMENT_UPDATED WebSocket notification for commentId: {}", 
                    event.getCommentId());
            
        } catch (Exception e) {
            log.error("Failed to send COMMENT_UPDATED WebSocket notification for commentId: {}", 
                     event.getCommentId(), e);
        }
    }
    
    /**
     * Обробляє подію видалення коментаря
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentDeleted(CommentDeletedEvent event) {
        log.debug("Processing COMMENT_DELETED event for commentId: {}, cardId: {}", 
                 event.getCommentId(), event.getCardId());
        
        try {
            WebSocketMessage<CommentRead> message = WebSocketMessage.<CommentRead>builder()
                .type(WebSocketMessage.WebSocketMessageType.COMMENT_DELETED)
                .payload(event.getCommentData())
                .entityId(event.getCommentId().toString())
                .build();
                
            webSocketService.sendBoardMessage(event.getBoardId(), message);
            webSocketService.sendListMessage(event.getListId(), message);
            webSocketService.sendCardMessage(event.getCardId(), message);
            
            log.info("Successfully sent COMMENT_DELETED WebSocket notification for commentId: {}", 
                    event.getCommentId());
            
        } catch (Exception e) {
            log.error("Failed to send COMMENT_DELETED WebSocket notification for commentId: {}", 
                     event.getCommentId(), e);
        }
    }
    
    /**
     * Обробляє помилки транзакцій (optional)
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void handleTransactionRollback(CommentCreatedEvent event) {
        log.warn("Transaction rolled back for COMMENT_CREATED event, commentId: {}", event.getCommentId());
    }
}
