package com.kyut.ordo.card.listener;

import com.kyut.ordo.card.dto.CardWithItsListRead;
import com.kyut.ordo.card.event.CardCreatedEvent;
import com.kyut.ordo.card.event.CardDeletedEvent;
import com.kyut.ordo.card.event.CardPositionsUpdatedEvent;
import com.kyut.ordo.card.event.CardUpdatedEvent;
import com.kyut.ordo.core.websocket.dto.WebSocketMessage;
import com.kyut.ordo.core.websocket.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CardEventListener {
    
    private final WebSocketService webSocketService;
    
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCardCreated(CardCreatedEvent event) {
        log.debug("Processing CARD_CREATED event for cardId: {}", event.getCardId());
        
        try {
            WebSocketMessage<CardWithItsListRead> message = WebSocketMessage.<CardWithItsListRead>builder()
                .type(WebSocketMessage.WebSocketMessageType.CARD_CREATED)
                .payload(event.getCardData())
                .entityId(event.getCardId().toString())
                .build();
                
            webSocketService.sendBoardMessage(event.getBoardId(), message);
            webSocketService.sendListMessage(event.getListId(), message);
            
            log.info("Successfully sent CARD_CREATED WebSocket notification for cardId: {}", 
                    event.getCardId());
            
        } catch (Exception e) {
            log.error("Failed to send CARD_CREATED WebSocket notification for cardId: {}", 
                     event.getCardId(), e);
        }
    }
    
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCardUpdated(CardUpdatedEvent event) {
        log.debug("Processing CARD_UPDATED event for cardId: {}", event.getCardId());
        
        try {
            WebSocketMessage<CardWithItsListRead> message = WebSocketMessage.<CardWithItsListRead>builder()
                .type(WebSocketMessage.WebSocketMessageType.CARD_UPDATED)
                .payload(event.getCardData())
                .entityId(event.getCardId().toString())
                .build();
                
            webSocketService.sendBoardMessage(event.getBoardId(), message);
            webSocketService.sendListMessage(event.getListId(), message);
            
            // Якщо картка переміщена між списками
            if (event.isListChanged() && event.getOldListId() != null) {
                webSocketService.sendListMessage(event.getOldListId(), message);
            }
            
            log.info("Successfully sent CARD_UPDATED WebSocket notification for cardId: {}", 
                    event.getCardId());
            
        } catch (Exception e) {
            log.error("Failed to send CARD_UPDATED WebSocket notification for cardId: {}", 
                     event.getCardId(), e);
        }
    }
    
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCardDeleted(CardDeletedEvent event) {
        log.debug("Processing CARD_DELETED event for cardId: {}", event.getCardId());
        
        try {
            WebSocketMessage<CardWithItsListRead> message = WebSocketMessage.<CardWithItsListRead>builder()
                .type(WebSocketMessage.WebSocketMessageType.CARD_DELETED)
                .payload(event.getCardData())
                .entityId(event.getCardId().toString())
                .build();
                
            webSocketService.sendBoardMessage(event.getBoardId(), message);
            webSocketService.sendListMessage(event.getListId(), message);
            
            log.info("Successfully sent CARD_DELETED WebSocket notification for cardId: {}", 
                    event.getCardId());
            
        } catch (Exception e) {
            log.error("Failed to send CARD_DELETED WebSocket notification for cardId: {}", 
                     event.getCardId(), e);
        }
    }
    
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCardPositionsUpdated(CardPositionsUpdatedEvent event) {
        log.debug("Processing CARD_POSITIONS_UPDATED event for listId: {}", event.getListId());
        
        try {
            WebSocketMessage<List<Long>> message = WebSocketMessage.<List<Long>>builder()
                .type(WebSocketMessage.WebSocketMessageType.CARD_POSITIONS_UPDATED)
                .payload(event.getCardIds())
                .entityId(event.getListId().toString())
                .build();
                
            webSocketService.sendBoardMessage(event.getBoardId(), message);
            webSocketService.sendListMessage(event.getListId(), message);
            
            log.info("Successfully sent CARD_POSITIONS_UPDATED WebSocket notification for listId: {}", 
                    event.getListId());
            
        } catch (Exception e) {
            log.error("Failed to send CARD_POSITIONS_UPDATED WebSocket notification for listId: {}", 
                     event.getListId(), e);
        }
    }
}
