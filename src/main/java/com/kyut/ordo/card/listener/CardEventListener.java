package com.kyut.ordo.card.listener;

import com.kyut.ordo.card.event.CardCreatedEvent;
import com.kyut.ordo.card.event.CardDeletedEvent;
import com.kyut.ordo.card.event.CardPositionsUpdatedEvent;
import com.kyut.ordo.card.event.CardUpdatedEvent;
import com.kyut.ordo.core.common.listener.BaseWebSocketEventListener;
import com.kyut.ordo.core.websocket.dto.WebSocketMessage;
import com.kyut.ordo.core.websocket.service.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@Slf4j
public class CardEventListener extends BaseWebSocketEventListener {
    
    public CardEventListener(WebSocketService webSocketService) {
        super(webSocketService);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCardCreated(CardCreatedEvent event) {
        String entityId = event.getCardId().toString();
        logEventProcessingStart(event, entityId);
        
        try {
            WebSocketMessage<com.kyut.ordo.card.dto.CardWithItsListRead> message = 
                WebSocketMessage.<com.kyut.ordo.card.dto.CardWithItsListRead>builder()
                    .type(WebSocketMessage.WebSocketMessageType.CARD_CREATED)
                    .payload(event.getCardData())
                    .entityId(entityId)
                    .build();
                    
            webSocketService.sendBoardMessage(event.getBoardId(), message);
            webSocketService.sendListMessage(event.getListId(), message);
            
            logEventProcessingSuccess(event, entityId);
            
        } catch (Exception e) {
            handleWebSocketError(event, entityId, e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCardUpdated(CardUpdatedEvent event) {
        String entityId = event.getCardId().toString();
        logEventProcessingStart(event, entityId);
        
        try {
            WebSocketMessage<com.kyut.ordo.card.dto.CardWithItsListRead> message = 
                WebSocketMessage.<com.kyut.ordo.card.dto.CardWithItsListRead>builder()
                    .type(WebSocketMessage.WebSocketMessageType.CARD_UPDATED)
                    .payload(event.getCardData())
                    .entityId(entityId)
                    .build();
                    
            webSocketService.sendBoardMessage(event.getBoardId(), message);
            webSocketService.sendListMessage(event.getListId(), message);
            
            // Якщо картка була переміщена між списками, сповіщуємо і старий список
            if (event.isListChanged() && event.getOldListId() != null) {
                webSocketService.sendListMessage(event.getOldListId(), message);
            }
            
            logEventProcessingSuccess(event, entityId);
            
        } catch (Exception e) {
            handleWebSocketError(event, entityId, e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCardDeleted(CardDeletedEvent event) {
        String entityId = event.getCardId().toString();
        logEventProcessingStart(event, entityId);
        
        try {
            WebSocketMessage<com.kyut.ordo.card.dto.CardWithItsListRead> message = 
                WebSocketMessage.<com.kyut.ordo.card.dto.CardWithItsListRead>builder()
                    .type(WebSocketMessage.WebSocketMessageType.CARD_DELETED)
                    .payload(event.getCardData())
                    .entityId(entityId)
                    .build();
                    
            webSocketService.sendBoardMessage(event.getBoardId(), message);
            webSocketService.sendListMessage(event.getListId(), message);
            
            logEventProcessingSuccess(event, entityId);
            
        } catch (Exception e) {
            handleWebSocketError(event, entityId, e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCardPositionsUpdated(CardPositionsUpdatedEvent event) {
        String entityId = event.getListId().toString();
        logEventProcessingStart(event, entityId);
        
        try {
            WebSocketMessage<List<Long>> message = WebSocketMessage.<List<Long>>builder()
                .type(WebSocketMessage.WebSocketMessageType.CARD_POSITIONS_UPDATED)
                .payload(event.getCardIds())
                .entityId(entityId)
                .build();
                
            webSocketService.sendBoardMessage(event.getBoardId(), message);
            webSocketService.sendListMessage(event.getListId(), message);
            
            logEventProcessingSuccess(event, entityId);
            
        } catch (Exception e) {
            handleWebSocketError(event, entityId, e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void handleTransactionRollback(CardCreatedEvent event) {
        log.warn("Transaction rolled back for {} event, cardId: {}, eventId: {}", 
                event.getEventType(), event.getCardId(), event.getEventId());
    }
}
