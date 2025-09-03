package com.kyut.ordo.feature.list.listener;

import com.kyut.ordo.feature.list.dto.ListRead;
import com.kyut.ordo.feature.list.event.ListCreatedEvent;
import com.kyut.ordo.feature.list.event.ListDeletedEvent;
import com.kyut.ordo.feature.list.event.ListPositionsUpdatedEvent;
import com.kyut.ordo.feature.list.event.ListUpdatedEvent;
import com.kyut.ordo.websocket.WebSocketMessage;
import com.kyut.ordo.websocket.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ListEventListener {
    
    private final WebSocketService webSocketService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleListCreated(ListCreatedEvent event) {
        log.debug("Processing LIST_CREATED event for listId: {}, boardId: {}", 
                 event.getListId(), event.getBoardId());
        
        try {
            WebSocketMessage<ListRead> message = WebSocketMessage.<ListRead>builder()
                .type(WebSocketMessage.WebSocketMessageType.LIST_CREATED)
                .payload(event.getListData())
                .entityId(event.getListId().toString())
                .build();
                
            webSocketService.sendBoardMessage(event.getBoardId(), message);
            
            log.info("Successfully sent LIST_CREATED WebSocket notification for listId: {}", 
                    event.getListId());
            
        } catch (Exception e) {
            log.error("Failed to send LIST_CREATED WebSocket notification for listId: {}", 
                     event.getListId(), e);
            // Не кидаємо exception - WebSocket помилки не повинні впливати на бізнес-логіку
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleListUpdated(ListUpdatedEvent event) {
        log.debug("Processing LIST_UPDATED event for listId: {}, boardId: {}", 
                 event.getListId(), event.getBoardId());
        
        try {
            WebSocketMessage<ListRead> message = WebSocketMessage.<ListRead>builder()
                .type(WebSocketMessage.WebSocketMessageType.LIST_UPDATED)
                .payload(event.getListData())
                .entityId(event.getListId().toString())
                .build();
                
            webSocketService.sendBoardMessage(event.getBoardId(), message);
            webSocketService.sendListMessage(event.getListId(), message);
            
            log.info("Successfully sent LIST_UPDATED WebSocket notification for listId: {}", 
                    event.getListId());
            
        } catch (Exception e) {
            log.error("Failed to send LIST_UPDATED WebSocket notification for listId: {}", 
                     event.getListId(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleListDeleted(ListDeletedEvent event) {
        log.debug("Processing LIST_DELETED event for listId: {}, boardId: {}", 
                 event.getListId(), event.getBoardId());
        
        try {
            WebSocketMessage<ListRead> message = WebSocketMessage.<ListRead>builder()
                .type(WebSocketMessage.WebSocketMessageType.LIST_DELETED)
                .payload(event.getListData())
                .entityId(event.getListId().toString())
                .build();
                
            webSocketService.sendBoardMessage(event.getBoardId(), message);
            webSocketService.sendListMessage(event.getListId(), message);
            
            log.info("Successfully sent LIST_DELETED WebSocket notification for listId: {}", 
                    event.getListId());
            
        } catch (Exception e) {
            log.error("Failed to send LIST_DELETED WebSocket notification for listId: {}", 
                     event.getListId(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleListPositionsUpdated(ListPositionsUpdatedEvent event) {
        log.debug("Processing LIST_POSITIONS_UPDATED event for boardId: {}, listIds: {}", 
                 event.getBoardId(), event.getListIds());
        
        try {
            WebSocketMessage<List<Long>> message = WebSocketMessage.<List<Long>>builder()
                .type(WebSocketMessage.WebSocketMessageType.LIST_POSITIONS_UPDATED)
                .payload(event.getListIds())
                .entityId(event.getBoardId().toString())
                .build();
                
            webSocketService.sendBoardMessage(event.getBoardId(), message);
            
            log.info("Successfully sent LIST_POSITIONS_UPDATED WebSocket notification for boardId: {}", 
                    event.getBoardId());
            
        } catch (Exception e) {
            log.error("Failed to send LIST_POSITIONS_UPDATED WebSocket notification for boardId: {}", 
                     event.getBoardId(), e);
        }
    }

}