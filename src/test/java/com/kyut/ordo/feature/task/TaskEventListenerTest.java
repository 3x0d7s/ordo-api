package com.kyut.ordo.feature.task;

import com.kyut.ordo.feature.task.dto.TaskRead;
import com.kyut.ordo.feature.task.event.TaskCreatedEvent;
import com.kyut.ordo.feature.task.event.TaskDeletedEvent;
import com.kyut.ordo.feature.task.event.TaskUpdatedEvent;
import com.kyut.ordo.feature.task.listener.TaskEventListener;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Unit tests for TaskEventListener
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TaskEventListener Unit Tests")
class TaskEventListenerTest {

    @Mock
    private WebSocketService webSocketService;

    @InjectMocks
    private TaskEventListener taskEventListener;

    @Captor
    private ArgumentCaptor<WebSocketMessage<TaskRead>> messageCaptor;

    private TaskRead taskReadDto;
    private Long taskId;
    private Long cardId;

    @BeforeEach
    void setUp() {
        taskId = 1L;
        cardId = 100L;

        taskReadDto = new TaskRead();
        taskReadDto.setId(taskId);
        taskReadDto.setTitle("Test Task");
        taskReadDto.setDescription("Test Task Description");
        taskReadDto.setPosition(0);
        taskReadDto.setCompleted(false);
    }

    @Test
    @DisplayName("Handle task created event - success")
    void handleTaskCreated_Success() {
        // Given
        TaskCreatedEvent event = new TaskCreatedEvent(taskId, cardId, taskReadDto);

        // When
        taskEventListener.handleTaskCreated(event);

        // Then
        verify(webSocketService).sendCardMessage(eq(cardId), messageCaptor.capture());
        
        WebSocketMessage<TaskRead> capturedMessage = messageCaptor.getValue();
        assertThat(capturedMessage.getType()).isEqualTo(WebSocketMessage.WebSocketMessageType.TASK_CREATED);
        assertThat(capturedMessage.getPayload()).isEqualTo(taskReadDto);
        assertThat(capturedMessage.getEntityId()).isEqualTo(taskId.toString());
    }

    @Test
    @DisplayName("Handle task created event - WebSocket service throws exception")
    void handleTaskCreated_WebSocketException() {
        // Given
        TaskCreatedEvent event = new TaskCreatedEvent(taskId, cardId, taskReadDto);
        doThrow(new RuntimeException("WebSocket error")).when(webSocketService)
            .sendCardMessage(eq(cardId), any());

        // When & Then - should not throw exception (error is logged)
        taskEventListener.handleTaskCreated(event);

        // Verify WebSocket service was called
        verify(webSocketService).sendCardMessage(eq(cardId), any());
    }

    @Test
    @DisplayName("Handle task updated event - success")
    void handleTaskUpdated_Success() {
        // Given
        TaskUpdatedEvent event = new TaskUpdatedEvent(taskId, cardId, taskReadDto);

        // When
        taskEventListener.handleTaskUpdated(event);

        // Then
        verify(webSocketService).sendCardMessage(eq(cardId), messageCaptor.capture());
        
        WebSocketMessage<TaskRead> capturedMessage = messageCaptor.getValue();
        assertThat(capturedMessage.getType()).isEqualTo(WebSocketMessage.WebSocketMessageType.TASK_UPDATED);
        assertThat(capturedMessage.getPayload()).isEqualTo(taskReadDto);
        assertThat(capturedMessage.getEntityId()).isEqualTo(taskId.toString());
    }

    @Test
    @DisplayName("Handle task updated event - WebSocket service throws exception")
    void handleTaskUpdated_WebSocketException() {
        // Given
        TaskUpdatedEvent event = new TaskUpdatedEvent(taskId, cardId, taskReadDto);
        doThrow(new RuntimeException("WebSocket error")).when(webSocketService)
            .sendCardMessage(eq(cardId), any());

        // When & Then - should not throw exception (error is logged)
        taskEventListener.handleTaskUpdated(event);

        // Verify WebSocket service was called
        verify(webSocketService).sendCardMessage(eq(cardId), any());
    }

    @Test
    @DisplayName("Handle task deleted event - success")
    void handleTaskDeleted_Success() {
        // Given
        TaskDeletedEvent event = new TaskDeletedEvent(taskId, cardId, taskReadDto);

        // When
        taskEventListener.handleTaskDeleted(event);

        // Then
        verify(webSocketService).sendCardMessage(eq(cardId), messageCaptor.capture());
        
        WebSocketMessage<TaskRead> capturedMessage = messageCaptor.getValue();
        assertThat(capturedMessage.getType()).isEqualTo(WebSocketMessage.WebSocketMessageType.TASK_DELETED);
        assertThat(capturedMessage.getPayload()).isEqualTo(taskReadDto);
        assertThat(capturedMessage.getEntityId()).isEqualTo(taskId.toString());
    }

    @Test
    @DisplayName("Handle task deleted event - WebSocket service throws exception")
    void handleTaskDeleted_WebSocketException() {
        // Given
        TaskDeletedEvent event = new TaskDeletedEvent(taskId, cardId, taskReadDto);
        doThrow(new RuntimeException("WebSocket error")).when(webSocketService)
            .sendCardMessage(eq(cardId), any());

        // When & Then - should not throw exception (error is logged)
        taskEventListener.handleTaskDeleted(event);

        // Verify WebSocket service was called
        verify(webSocketService).sendCardMessage(eq(cardId), any());
    }

    @Test
    @DisplayName("Handle transaction rollback - success")
    void handleTransactionRollback_Success() {
        // Given
        TaskCreatedEvent event = new TaskCreatedEvent(taskId, cardId, taskReadDto);

        // When
        taskEventListener.handleTransactionRollback(event);

        // Then - should just log, no WebSocket calls
        verifyNoInteractions(webSocketService);
    }

    @Test
    @DisplayName("Handle task created with null data - should handle gracefully")
    void handleTaskCreated_WithNullData() {
        // Given
        TaskCreatedEvent event = new TaskCreatedEvent(taskId, cardId, null);

        // When
        taskEventListener.handleTaskCreated(event);

        // Then
        verify(webSocketService).sendCardMessage(eq(cardId), messageCaptor.capture());
        
        WebSocketMessage<TaskRead> capturedMessage = messageCaptor.getValue();
        assertThat(capturedMessage.getType()).isEqualTo(WebSocketMessage.WebSocketMessageType.TASK_CREATED);
        assertThat(capturedMessage.getPayload()).isNull();
        assertThat(capturedMessage.getEntityId()).isEqualTo(taskId.toString());
    }

    @Test
    @DisplayName("Handle task updated with different card ID - should use event card ID")
    void handleTaskUpdated_WithDifferentCardId() {
        // Given
        Long differentCardId = 200L;
        TaskUpdatedEvent event = new TaskUpdatedEvent(taskId, differentCardId, taskReadDto);

        // When
        taskEventListener.handleTaskUpdated(event);

        // Then
        verify(webSocketService).sendCardMessage(eq(differentCardId), messageCaptor.capture());
        
        WebSocketMessage<TaskRead> capturedMessage = messageCaptor.getValue();
        assertThat(capturedMessage.getType()).isEqualTo(WebSocketMessage.WebSocketMessageType.TASK_UPDATED);
        assertThat(capturedMessage.getPayload()).isEqualTo(taskReadDto);
        assertThat(capturedMessage.getEntityId()).isEqualTo(taskId.toString());
    }

    @Test
    @DisplayName("Handle multiple events - should process all successfully")
    void handleMultipleEvents_Success() {
        // Given
        TaskCreatedEvent createdEvent = new TaskCreatedEvent(taskId, cardId, taskReadDto);
        TaskUpdatedEvent updatedEvent = new TaskUpdatedEvent(taskId, cardId, taskReadDto);
        TaskDeletedEvent deletedEvent = new TaskDeletedEvent(taskId, cardId, taskReadDto);

        // When
        taskEventListener.handleTaskCreated(createdEvent);
        taskEventListener.handleTaskUpdated(updatedEvent);
        taskEventListener.handleTaskDeleted(deletedEvent);

        // Then - Verify all three calls were made (3 times total)
        verify(webSocketService, times(3)).sendCardMessage(eq(cardId), any());
    }
}
