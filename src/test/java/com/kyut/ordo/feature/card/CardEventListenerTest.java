package com.kyut.ordo.feature.card;

import com.kyut.ordo.feature.card.dto.CardWithItsListRead;
import com.kyut.ordo.feature.card.event.CardCreatedEvent;
import com.kyut.ordo.feature.card.event.CardDeletedEvent;
import com.kyut.ordo.feature.card.event.CardPositionsUpdatedEvent;
import com.kyut.ordo.feature.card.event.CardUpdatedEvent;
import com.kyut.ordo.feature.card.listener.CardEventListener;
import com.kyut.ordo.websocket.WebSocketMessage;
import com.kyut.ordo.websocket.WebSocketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.anyLong;

/**
 * Unit tests for CardEventListener
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CardEventListener Unit Tests")
class CardEventListenerTest {

    @Mock
    private WebSocketService webSocketService;

    @InjectMocks
    private CardEventListener cardEventListener;

    private CardWithItsListRead testCardData;
    private Long boardId;
    private Long listId;
    private Long cardId;

    @BeforeEach
    void setUp() {
        boardId = 1L;
        listId = 2L;
        cardId = 3L;

        testCardData = new CardWithItsListRead();
        testCardData.setId(cardId);
        testCardData.setTitle("Test Card");
        testCardData.setDescription("Test Description");
        testCardData.setDueDate(LocalDate.now().plusDays(7));
        testCardData.setPosition(0);
        testCardData.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Handle card created event - should send WebSocket messages to board and list")
    void handleCardCreated_ShouldSendWebSocketMessages() {
        // Given
        CardCreatedEvent event = new CardCreatedEvent(cardId, listId, boardId, testCardData);

        // When
        cardEventListener.handleCardCreated(event);

        // Then
        ArgumentCaptor<WebSocketMessage> messageCaptor = ArgumentCaptor.forClass(WebSocketMessage.class);

        verify(webSocketService).sendBoardMessage(eq(boardId), messageCaptor.capture());
        verify(webSocketService).sendListMessage(eq(listId), messageCaptor.capture());

        List<WebSocketMessage> capturedMessages = messageCaptor.getAllValues();
        assertThat(capturedMessages).hasSize(2);

        // Verify first message (board message)
        WebSocketMessage boardMessage = capturedMessages.get(0);
        assertThat(boardMessage.getType()).isEqualTo(WebSocketMessage.WebSocketMessageType.CARD_CREATED);
        assertThat(boardMessage.getPayload()).isEqualTo(testCardData);
        assertThat(boardMessage.getEntityId()).isEqualTo(cardId.toString());

        // Verify second message (list message)
        WebSocketMessage listMessage = capturedMessages.get(1);
        assertThat(listMessage.getType()).isEqualTo(WebSocketMessage.WebSocketMessageType.CARD_CREATED);
        assertThat(listMessage.getPayload()).isEqualTo(testCardData);
        assertThat(listMessage.getEntityId()).isEqualTo(cardId.toString());
    }

    @Test
    @DisplayName("Handle card created event - should handle WebSocket service exception gracefully")
    void handleCardCreated_ShouldHandleWebSocketExceptionGracefully() {
        // Given
        CardCreatedEvent event = new CardCreatedEvent(cardId, listId, boardId, testCardData);
        doThrow(new RuntimeException("WebSocket error")).when(webSocketService)
            .sendBoardMessage(eq(boardId), any());

        // When & Then - should not throw exception
        cardEventListener.handleCardCreated(event);

        // Verify sendBoardMessage was called and failed
        verify(webSocketService).sendBoardMessage(eq(boardId), any());
        // sendListMessage should NOT be called when sendBoardMessage throws exception
        verify(webSocketService, never()).sendListMessage(anyLong(), any());
    }

    @Test
    @DisplayName("Handle card updated event - should send WebSocket messages")
    void handleCardUpdated_ShouldSendWebSocketMessages() {
        // Given
        CardUpdatedEvent event = new CardUpdatedEvent(cardId, listId, boardId, testCardData);

        // When
        cardEventListener.handleCardUpdated(event);

        // Then
        ArgumentCaptor<WebSocketMessage> messageCaptor = ArgumentCaptor.forClass(WebSocketMessage.class);

        verify(webSocketService).sendBoardMessage(eq(boardId), messageCaptor.capture());
        verify(webSocketService).sendListMessage(eq(listId), messageCaptor.capture());

        List<WebSocketMessage> capturedMessages = messageCaptor.getAllValues();
        assertThat(capturedMessages).hasSize(2);

        WebSocketMessage message = capturedMessages.get(0);
        assertThat(message.getType()).isEqualTo(WebSocketMessage.WebSocketMessageType.CARD_UPDATED);
        assertThat(message.getPayload()).isEqualTo(testCardData);
        assertThat(message.getEntityId()).isEqualTo(cardId.toString());
    }

    @Test
    @DisplayName("Handle card updated event with list change - should send message to old list")
    void handleCardUpdated_WithListChange_ShouldSendMessageToOldList() {
        // Given
        Long oldListId = 99L;
        CardUpdatedEvent event = new CardUpdatedEvent(cardId, listId, boardId, oldListId, testCardData, true);

        // When
        cardEventListener.handleCardUpdated(event);

        // Then
        verify(webSocketService).sendBoardMessage(eq(boardId), any());
        verify(webSocketService).sendListMessage(eq(listId), any());
        verify(webSocketService).sendListMessage(eq(oldListId), any());
        
        // Total of 2 calls to sendListMessage: 1 to new list, 1 to old list
        verify(webSocketService, times(2)).sendListMessage(anyLong(), any());
    }

    @Test
    @DisplayName("Handle card updated event without list change - should not send message to old list")
    void handleCardUpdated_WithoutListChange_ShouldNotSendMessageToOldList() {
        // Given
        CardUpdatedEvent event = new CardUpdatedEvent(cardId, listId, boardId, null, testCardData, false);

        // When
        cardEventListener.handleCardUpdated(event);

        // Then
        verify(webSocketService).sendBoardMessage(eq(boardId), any());
        verify(webSocketService).sendListMessage(eq(listId), any());
        
        // Total of 1 call to sendListMessage: 1 to current list only, 0 to old list
        verify(webSocketService, times(1)).sendListMessage(anyLong(), any());
    }

    @Test
    @DisplayName("Handle card deleted event - should send WebSocket messages")
    void handleCardDeleted_ShouldSendWebSocketMessages() {
        // Given
        CardDeletedEvent event = new CardDeletedEvent(cardId, listId, boardId, testCardData);

        // When
        cardEventListener.handleCardDeleted(event);

        // Then
        ArgumentCaptor<WebSocketMessage> messageCaptor = ArgumentCaptor.forClass(WebSocketMessage.class);

        verify(webSocketService).sendBoardMessage(eq(boardId), messageCaptor.capture());
        verify(webSocketService).sendListMessage(eq(listId), messageCaptor.capture());

        List<WebSocketMessage> capturedMessages = messageCaptor.getAllValues();
        assertThat(capturedMessages).hasSize(2);

        WebSocketMessage message = capturedMessages.get(0);
        assertThat(message.getType()).isEqualTo(WebSocketMessage.WebSocketMessageType.CARD_DELETED);
        assertThat(message.getPayload()).isEqualTo(testCardData);
        assertThat(message.getEntityId()).isEqualTo(cardId.toString());
    }

    @Test
    @DisplayName("Handle card positions updated event - should send WebSocket messages")
    void handleCardPositionsUpdated_ShouldSendWebSocketMessages() {
        // Given
        List<Long> cardIds = List.of(1L, 2L, 3L);
        CardPositionsUpdatedEvent event = new CardPositionsUpdatedEvent(listId, boardId, cardIds);

        // When
        cardEventListener.handleCardPositionsUpdated(event);

        // Then
        ArgumentCaptor<WebSocketMessage> messageCaptor = ArgumentCaptor.forClass(WebSocketMessage.class);

        verify(webSocketService).sendBoardMessage(eq(boardId), messageCaptor.capture());
        verify(webSocketService).sendListMessage(eq(listId), messageCaptor.capture());

        List<WebSocketMessage> capturedMessages = messageCaptor.getAllValues();
        assertThat(capturedMessages).hasSize(2);

        WebSocketMessage message = capturedMessages.get(0);
        assertThat(message.getType()).isEqualTo(WebSocketMessage.WebSocketMessageType.CARD_POSITIONS_UPDATED);
        assertThat(message.getPayload()).isEqualTo(cardIds);
        assertThat(message.getEntityId()).isEqualTo(listId.toString());
    }

    @Test
    @DisplayName("Handle card positions updated event - should handle exception gracefully")
    void handleCardPositionsUpdated_ShouldHandleExceptionGracefully() {
        // Given
        List<Long> cardIds = List.of(1L, 2L, 3L);
        CardPositionsUpdatedEvent event = new CardPositionsUpdatedEvent(listId, boardId, cardIds);
        
        doThrow(new RuntimeException("WebSocket error")).when(webSocketService)
            .sendBoardMessage(eq(boardId), any());

        // When & Then - should not throw exception
        cardEventListener.handleCardPositionsUpdated(event);

        // Verify sendBoardMessage was called and failed
        verify(webSocketService).sendBoardMessage(eq(boardId), any());
        // sendListMessage should NOT be called when sendBoardMessage throws exception
        verify(webSocketService, never()).sendListMessage(anyLong(), any());
    }

    @Test
    @DisplayName("All event handlers should be transactional listeners")
    void allEventHandlers_ShouldBeTransactionalListeners() {
        // This test verifies that all event handler methods are properly annotated
        // with @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
        // This is important for ensuring events are only sent after successful database commits
        
        // Given events
        CardCreatedEvent createdEvent = new CardCreatedEvent(cardId, listId, boardId, testCardData);
        CardUpdatedEvent updatedEvent = new CardUpdatedEvent(cardId, listId, boardId, testCardData);
        CardDeletedEvent deletedEvent = new CardDeletedEvent(cardId, listId, boardId, testCardData);
        CardPositionsUpdatedEvent positionsEvent = new CardPositionsUpdatedEvent(listId, boardId, List.of(cardId));

        // When - call all handlers
        cardEventListener.handleCardCreated(createdEvent);
        cardEventListener.handleCardUpdated(updatedEvent);
        cardEventListener.handleCardDeleted(deletedEvent);
        cardEventListener.handleCardPositionsUpdated(positionsEvent);

        // Then - verify all called WebSocket service (indicating proper method execution)
        verify(webSocketService, times(4)).sendBoardMessage(anyLong(), any());
        verify(webSocketService, times(4)).sendListMessage(anyLong(), any());
    }
}
