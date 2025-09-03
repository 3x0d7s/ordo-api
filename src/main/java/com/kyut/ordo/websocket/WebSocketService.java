package com.kyut.ordo.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketService {
    private final SimpMessagingTemplate messagingTemplate;

    public <T> void sendBoardMessage(Long boardId, WebSocketMessage<T> message) {
        message.setTimestamp(System.currentTimeMillis());
        messagingTemplate.convertAndSend("/topic/board/" + boardId, message);
    }

    public <T> void sendListMessage(Long listId, WebSocketMessage<T> message) {
        message.setTimestamp(System.currentTimeMillis());
        messagingTemplate.convertAndSend("/topic/list/" + listId, message);
    }

    public <T> void sendCardMessage(Long cardId, WebSocketMessage<T> message) {
        message.setTimestamp(System.currentTimeMillis());
        messagingTemplate.convertAndSend("/topic/card/" + cardId, message);
    }
}
