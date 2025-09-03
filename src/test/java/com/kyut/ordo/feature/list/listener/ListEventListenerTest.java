package com.kyut.ordo.feature.list.listener;

import com.kyut.ordo.websocket.WebSocketMessage;
import com.kyut.ordo.websocket.WebSocketService;
import com.kyut.ordo.feature.list.dto.ListRead;
import com.kyut.ordo.feature.list.event.ListCreatedEvent;
import com.kyut.ordo.feature.list.event.ListDeletedEvent;
import com.kyut.ordo.feature.list.event.ListUpdatedEvent;
import com.kyut.ordo.feature.list.listener.ListEventListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

/**
 * Unit тести для ListEventListener
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ListEventListener Unit Tests")
class ListEventListenerTest {

    @Mock
    private WebSocketService webSocketService;

    @InjectMocks
    private ListEventListener listEventListener;

    private ListRead testListRead;

    @BeforeEach
    void setUp() {
        testListRead = new ListRead();
        testListRead.setId(1L);
        testListRead.setTitle("Test List");
        testListRead.setPosition(0);
    }

    @Test
    @DisplayName("Обробка події створення списку")
    void handleListCreated() {
        // Given
        ListCreatedEvent event = new ListCreatedEvent(1L, 10L, testListRead);

        // When
        listEventListener.handleListCreated(event);

        // Then
        ArgumentCaptor<WebSocketMessage<ListRead>> messageCaptor = ArgumentCaptor.forClass(WebSocketMessage.class);
        verify(webSocketService).sendBoardMessage(eq(10L), messageCaptor.capture());

        WebSocketMessage<ListRead> capturedMessage = messageCaptor.getValue();
        assertThat(capturedMessage.getType()).isEqualTo(WebSocketMessage.WebSocketMessageType.LIST_CREATED);
        assertThat(capturedMessage.getPayload()).isEqualTo(testListRead);
        assertThat(capturedMessage.getEntityId()).isEqualTo("1");
        // Timestamp встановлюється в WebSocketService.sendBoardMessage(), тому тут він ще null
        // assertThat(capturedMessage.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Обробка події оновлення списку")
    void handleListUpdated() {
        // Given
        ListUpdatedEvent event = new ListUpdatedEvent(1L, 10L, testListRead);

        // When
        listEventListener.handleListUpdated(event);

        // Then
        verify(webSocketService).sendBoardMessage(eq(10L), any(WebSocketMessage.class));
        verify(webSocketService).sendListMessage(eq(1L), any(WebSocketMessage.class));
        
        ArgumentCaptor<WebSocketMessage<ListRead>> messageCaptor = ArgumentCaptor.forClass(WebSocketMessage.class);
        verify(webSocketService).sendBoardMessage(eq(10L), messageCaptor.capture());
        
        WebSocketMessage<ListRead> capturedMessage = messageCaptor.getValue();
        assertThat(capturedMessage.getType()).isEqualTo(WebSocketMessage.WebSocketMessageType.LIST_UPDATED);
    }

    @Test
    @DisplayName("Обробка події видалення списку")
    void handleListDeleted() {
        // Given
        ListDeletedEvent event = new ListDeletedEvent(1L, 10L, testListRead);

        // When
        listEventListener.handleListDeleted(event);

        // Then
        verify(webSocketService).sendBoardMessage(eq(10L), any(WebSocketMessage.class));
        verify(webSocketService).sendListMessage(eq(1L), any(WebSocketMessage.class));
        
        ArgumentCaptor<WebSocketMessage<ListRead>> messageCaptor = ArgumentCaptor.forClass(WebSocketMessage.class);
        verify(webSocketService).sendBoardMessage(eq(10L), messageCaptor.capture());
        
        WebSocketMessage<ListRead> capturedMessage = messageCaptor.getValue();
        assertThat(capturedMessage.getType()).isEqualTo(WebSocketMessage.WebSocketMessageType.LIST_DELETED);
    }

    @Test
    @DisplayName("Обробка помилки WebSocket - не повинна кидати exception")
    void handleListCreated_WebSocketError() {
        // Given
        ListCreatedEvent event = new ListCreatedEvent(1L, 10L, testListRead);
        doThrow(new RuntimeException("WebSocket connection failed"))
            .when(webSocketService).sendBoardMessage(anyLong(), any());

        // When & Then - не повинно кинути exception
        assertThatCode(() -> listEventListener.handleListCreated(event))
            .doesNotThrowAnyException();
    }
}
