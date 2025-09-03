package com.kyut.ordo.feature.task.listener;

import com.kyut.ordo.websocket.WebSocketMessage;
import com.kyut.ordo.websocket.WebSocketService;
import com.kyut.ordo.feature.task.dto.TaskRead;
import com.kyut.ordo.feature.task.event.TaskCreatedEvent;
import com.kyut.ordo.feature.task.event.TaskDeletedEvent;
import com.kyut.ordo.feature.task.event.TaskUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class TaskEventListener {
    
    private final WebSocketService webSocketService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskCreated(TaskCreatedEvent event) {
        log.debug("Processing TASK_CREATED event for taskId: {}, cardId: {}", 
                 event.getTaskId(), event.getCardId());
        
        try {
            WebSocketMessage<TaskRead> message = WebSocketMessage.<TaskRead>builder()
                .type(WebSocketMessage.WebSocketMessageType.TASK_CREATED)
                .payload(event.getTaskData())
                .entityId(event.getTaskId().toString())
                .build();
                
            webSocketService.sendCardMessage(event.getCardId(), message);
            
            log.info("Successfully sent TASK_CREATED WebSocket notification for taskId: {}", 
                    event.getTaskId());
            
        } catch (Exception e) {
            log.error("Failed to send TASK_CREATED WebSocket notification for taskId: {}", 
                     event.getTaskId(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskUpdated(TaskUpdatedEvent event) {
        log.debug("Processing TASK_UPDATED event for taskId: {}, cardId: {}", 
                 event.getTaskId(), event.getCardId());
        
        try {
            WebSocketMessage<TaskRead> message = WebSocketMessage.<TaskRead>builder()
                .type(WebSocketMessage.WebSocketMessageType.TASK_UPDATED)
                .payload(event.getTaskData())
                .entityId(event.getTaskId().toString())
                .build();
                
            webSocketService.sendCardMessage(event.getCardId(), message);
            
            log.info("Successfully sent TASK_UPDATED WebSocket notification for taskId: {}", 
                    event.getTaskId());
            
        } catch (Exception e) {
            log.error("Failed to send TASK_UPDATED WebSocket notification for taskId: {}", 
                     event.getTaskId(), e);
        }
    }
    
    /**
     * Обробляє подію видалення завдання
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskDeleted(TaskDeletedEvent event) {
        log.debug("Processing TASK_DELETED event for taskId: {}, cardId: {}", 
                 event.getTaskId(), event.getCardId());
        
        try {
            WebSocketMessage<TaskRead> message = WebSocketMessage.<TaskRead>builder()
                .type(WebSocketMessage.WebSocketMessageType.TASK_DELETED)
                .payload(event.getTaskData())
                .entityId(event.getTaskId().toString())
                .build();
                
            webSocketService.sendCardMessage(event.getCardId(), message);
            
            log.info("Successfully sent TASK_DELETED WebSocket notification for taskId: {}", 
                    event.getTaskId());
            
        } catch (Exception e) {
            log.error("Failed to send TASK_DELETED WebSocket notification for taskId: {}", 
                     event.getTaskId(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void handleTransactionRollback(TaskCreatedEvent event) {
        log.warn("Transaction rolled back for TASK_CREATED event, taskId: {}", event.getTaskId());
    }
}
