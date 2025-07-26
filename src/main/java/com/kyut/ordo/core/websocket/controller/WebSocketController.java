package com.kyut.ordo.core.websocket.controller;

import com.kyut.ordo.core.websocket.dto.WebSocketMessage;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

//@Controller
@RequiredArgsConstructor
public class WebSocketController {
    @MessageMapping("/board/{boardId}")
    @SendTo("/topic/board/{boardId}")
    public WebSocketMessage<?> boardMessage(@DestinationVariable Long boardId, WebSocketMessage<?> message) {
        message.setTimestamp(System.currentTimeMillis());
        return message;
    }

    @MessageMapping("/list/{listId}")
    @SendTo("/topic/list/{listId}")
    public WebSocketMessage<?> listMessage(@DestinationVariable Long listId, WebSocketMessage<?> message) {
        message.setTimestamp(System.currentTimeMillis());
        return message;
    }

    @MessageMapping("/card/{cardId}")
    @SendTo("/topic/card/{cardId}")
    public WebSocketMessage<?> cardMessage(@DestinationVariable Long cardId, WebSocketMessage<?> message) {
        message.setTimestamp(System.currentTimeMillis());
        return message;
    }
}
