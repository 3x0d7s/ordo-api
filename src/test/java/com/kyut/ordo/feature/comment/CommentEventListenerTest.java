package com.kyut.ordo.feature.comment;

import com.kyut.ordo.feature.comment.dto.CommentRead;
import com.kyut.ordo.feature.comment.event.CommentCreatedEvent;
import com.kyut.ordo.feature.comment.event.CommentDeletedEvent;
import com.kyut.ordo.feature.comment.event.CommentUpdatedEvent;
import com.kyut.ordo.feature.comment.listener.CommentEventListener;
import com.kyut.ordo.websocket.WebSocketMessage;
import com.kyut.ordo.websocket.WebSocketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Unit tests for CommentEventListener
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CommentEventListener Unit Tests")
class CommentEventListenerTest {

    @Mock
    private WebSocketService webSocketService;

    @InjectMocks
    private CommentEventListener commentEventListener;

    @Captor
    private ArgumentCaptor<WebSocketMessage<CommentRead>> messageCaptor;

    private CommentRead commentReadDto;
    private Long commentId;
    private Long cardId;
    private Long listId;
    private Long boardId;

    @BeforeEach
    void setUp() {
        commentId = 1L;
        cardId = 100L;
        listId = 200L;
        boardId = 300L;

        commentReadDto = new CommentRead();
        commentReadDto.setId(commentId);
        commentReadDto.setMessage("Test Comment Message");
        commentReadDto.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Handle comment created event - success")
    void handleCommentCreated_Success() {
        // Given
        CommentCreatedEvent event = CommentCreatedEvent.builder()
            .commentId(commentId)
            .cardId(cardId)
            .listId(listId)
            .boardId(boardId)
            .commentData(commentReadDto)
            .build();

        // When
        commentEventListener.handleCommentCreated(event);

        // Then
        verify(webSocketService).sendBoardMessage(eq(boardId), messageCaptor.capture());
        verify(webSocketService).sendListMessage(eq(listId), any());
        verify(webSocketService).sendCardMessage(eq(cardId), any());
        
        WebSocketMessage<CommentRead> capturedMessage = messageCaptor.getValue();
        assertThat(capturedMessage.getType()).isEqualTo(WebSocketMessage.WebSocketMessageType.COMMENT_CREATED);
        assertThat(capturedMessage.getPayload()).isEqualTo(commentReadDto);
        assertThat(capturedMessage.getEntityId()).isEqualTo(commentId.toString());
    }

    @Test
    @DisplayName("Handle comment created event - WebSocket service throws exception")
    void handleCommentCreated_WebSocketException() {
        // Given
        CommentCreatedEvent event = CommentCreatedEvent.builder()
            .commentId(commentId)
            .cardId(cardId)
            .listId(listId)
            .boardId(boardId)
            .commentData(commentReadDto)
            .build();
        doThrow(new RuntimeException("WebSocket error")).when(webSocketService)
            .sendBoardMessage(eq(boardId), any());

        // When & Then - should not throw exception (error is logged)
        commentEventListener.handleCommentCreated(event);

        // Verify WebSocket service was called
        verify(webSocketService).sendBoardMessage(eq(boardId), any());
    }

    @Test
    @DisplayName("Handle comment updated event - success")
    void handleCommentUpdated_Success() {
        // Given
        CommentUpdatedEvent event = CommentUpdatedEvent.builder()
            .commentId(commentId)
            .cardId(cardId)
            .listId(listId)
            .boardId(boardId)
            .commentData(commentReadDto)
            .build();

        // When
        commentEventListener.handleCommentUpdated(event);

        // Then
        verify(webSocketService).sendBoardMessage(eq(boardId), messageCaptor.capture());
        verify(webSocketService).sendListMessage(eq(listId), any());
        verify(webSocketService).sendCardMessage(eq(cardId), any());
        
        WebSocketMessage<CommentRead> capturedMessage = messageCaptor.getValue();
        assertThat(capturedMessage.getType()).isEqualTo(WebSocketMessage.WebSocketMessageType.COMMENT_UPDATED);
        assertThat(capturedMessage.getPayload()).isEqualTo(commentReadDto);
        assertThat(capturedMessage.getEntityId()).isEqualTo(commentId.toString());
    }

    @Test
    @DisplayName("Handle comment updated event - WebSocket service throws exception")
    void handleCommentUpdated_WebSocketException() {
        // Given
        CommentUpdatedEvent event = CommentUpdatedEvent.builder()
            .commentId(commentId)
            .cardId(cardId)
            .listId(listId)
            .boardId(boardId)
            .commentData(commentReadDto)
            .build();
        doThrow(new RuntimeException("WebSocket error")).when(webSocketService)
            .sendBoardMessage(eq(boardId), any());

        // When & Then - should not throw exception (error is logged)
        commentEventListener.handleCommentUpdated(event);

        // Verify WebSocket service was called
        verify(webSocketService).sendBoardMessage(eq(boardId), any());
    }

    @Test
    @DisplayName("Handle comment deleted event - success")
    void handleCommentDeleted_Success() {
        // Given
        CommentDeletedEvent event = CommentDeletedEvent.builder()
            .commentId(commentId)
            .cardId(cardId)
            .listId(listId)
            .boardId(boardId)
            .commentData(commentReadDto)
            .build();

        // When
        commentEventListener.handleCommentDeleted(event);

        // Then
        verify(webSocketService).sendBoardMessage(eq(boardId), messageCaptor.capture());
        verify(webSocketService).sendListMessage(eq(listId), any());
        verify(webSocketService).sendCardMessage(eq(cardId), any());
        
        WebSocketMessage<CommentRead> capturedMessage = messageCaptor.getValue();
        assertThat(capturedMessage.getType()).isEqualTo(WebSocketMessage.WebSocketMessageType.COMMENT_DELETED);
        assertThat(capturedMessage.getPayload()).isEqualTo(commentReadDto);
        assertThat(capturedMessage.getEntityId()).isEqualTo(commentId.toString());
    }

    @Test
    @DisplayName("Handle comment deleted event - WebSocket service throws exception")
    void handleCommentDeleted_WebSocketException() {
        // Given
        CommentDeletedEvent event = CommentDeletedEvent.builder()
            .commentId(commentId)
            .cardId(cardId)
            .listId(listId)
            .boardId(boardId)
            .commentData(commentReadDto)
            .build();
        doThrow(new RuntimeException("WebSocket error")).when(webSocketService)
            .sendBoardMessage(eq(boardId), any());

        // When & Then - should not throw exception (error is logged)
        commentEventListener.handleCommentDeleted(event);

        // Verify WebSocket service was called
        verify(webSocketService).sendBoardMessage(eq(boardId), any());
    }

    @Test
    @DisplayName("Handle transaction rollback - success")
    void handleTransactionRollback_Success() {
        // Given
        CommentCreatedEvent event = CommentCreatedEvent.builder()
            .commentId(commentId)
            .cardId(cardId)
            .listId(listId)
            .boardId(boardId)
            .commentData(commentReadDto)
            .build();

        // When
        commentEventListener.handleTransactionRollback(event);

        // Then - should just log, no WebSocket calls
        verifyNoInteractions(webSocketService);
    }

    @Test
    @DisplayName("Handle comment created with null data - should handle gracefully")
    void handleCommentCreated_WithNullData() {
        // Given
        CommentCreatedEvent event = CommentCreatedEvent.builder()
            .commentId(commentId)
            .cardId(cardId)
            .listId(listId)
            .boardId(boardId)
            .commentData(null)
            .build();

        // When
        commentEventListener.handleCommentCreated(event);

        // Then
        verify(webSocketService).sendBoardMessage(eq(boardId), messageCaptor.capture());
        verify(webSocketService).sendListMessage(eq(listId), any());
        verify(webSocketService).sendCardMessage(eq(cardId), any());
        
        WebSocketMessage<CommentRead> capturedMessage = messageCaptor.getValue();
        assertThat(capturedMessage.getType()).isEqualTo(WebSocketMessage.WebSocketMessageType.COMMENT_CREATED);
        assertThat(capturedMessage.getPayload()).isNull();
        assertThat(capturedMessage.getEntityId()).isEqualTo(commentId.toString());
    }

    @Test
    @DisplayName("Handle comment events with different IDs - should use correct IDs")
    void handleCommentEvents_WithDifferentIds() {
        // Given
        Long differentCardId = 999L;
        Long differentListId = 888L;
        Long differentBoardId = 777L;
        
        CommentUpdatedEvent event = CommentUpdatedEvent.builder()
            .commentId(commentId)
            .cardId(differentCardId)
            .listId(differentListId)
            .boardId(differentBoardId)
            .commentData(commentReadDto)
            .build();

        // When
        commentEventListener.handleCommentUpdated(event);

        // Then
        verify(webSocketService).sendBoardMessage(eq(differentBoardId), any());
        verify(webSocketService).sendListMessage(eq(differentListId), any());
        verify(webSocketService).sendCardMessage(eq(differentCardId), any());
    }

    @Test
    @DisplayName("Handle multiple comment events - should process all successfully")
    void handleMultipleCommentEvents_Success() {
        // Given
        CommentCreatedEvent createdEvent = CommentCreatedEvent.builder()
            .commentId(commentId)
            .cardId(cardId)
            .listId(listId)
            .boardId(boardId)
            .commentData(commentReadDto)
            .build();
            
        CommentUpdatedEvent updatedEvent = CommentUpdatedEvent.builder()
            .commentId(commentId)
            .cardId(cardId)
            .listId(listId)
            .boardId(boardId)
            .commentData(commentReadDto)
            .build();
            
        CommentDeletedEvent deletedEvent = CommentDeletedEvent.builder()
            .commentId(commentId)
            .cardId(cardId)
            .listId(listId)
            .boardId(boardId)
            .commentData(commentReadDto)
            .build();

        // When
        commentEventListener.handleCommentCreated(createdEvent);
        commentEventListener.handleCommentUpdated(updatedEvent);
        commentEventListener.handleCommentDeleted(deletedEvent);

        // Then - Verify all calls were made (3 events × 3 destinations = 9 calls total)
        verify(webSocketService, times(3)).sendBoardMessage(eq(boardId), any());
        verify(webSocketService, times(3)).sendListMessage(eq(listId), any());
        verify(webSocketService, times(3)).sendCardMessage(eq(cardId), any());
    }

    @Test
    @DisplayName("Handle comment events send to multiple destinations")
    void handleCommentEvent_SendsToMultipleDestinations() {
        // Given
        CommentCreatedEvent event = CommentCreatedEvent.builder()
            .commentId(commentId)
            .cardId(cardId)
            .listId(listId)
            .boardId(boardId)
            .commentData(commentReadDto)
            .build();

        // When
        commentEventListener.handleCommentCreated(event);

        // Then - Should send to board, list, and card
        verify(webSocketService).sendBoardMessage(eq(boardId), any());
        verify(webSocketService).sendListMessage(eq(listId), any());
        verify(webSocketService).sendCardMessage(eq(cardId), any());
    }
}
