package com.kyut.ordo.core.websocket.controller;

import com.kyut.ordo.core.websocket.dto.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

/**
 * Контролер для обробки веб-сокет повідомлень
 */
@Controller
@RequiredArgsConstructor
public class WebSocketController {

    /**
     * Обробляє повідомлення, надіслані на /app/board/{boardId}
     * та пересилає їх всім підписникам на /topic/board/{boardId}
     *
     * @param boardId ID дошки
     * @param message повідомлення для пересилання
     * @return повідомлення для пересилання
     */
    @MessageMapping("/board/{boardId}")
    @SendTo("/topic/board/{boardId}")
    public WebSocketMessage<?> boardMessage(@DestinationVariable Long boardId, WebSocketMessage<?> message) {
        message.setTimestamp(System.currentTimeMillis());
        return message;
    }

    /**
     * Обробляє повідомлення, надіслані на /app/list/{listId}
     * та пересилає їх всім підписникам на /topic/list/{listId}
     *
     * @param listId ID списку
     * @param message повідомлення для пересилання
     * @return повідомлення для пересилання
     */
    @MessageMapping("/list/{listId}")
    @SendTo("/topic/list/{listId}")
    public WebSocketMessage<?> listMessage(@DestinationVariable Long listId, WebSocketMessage<?> message) {
        message.setTimestamp(System.currentTimeMillis());
        return message;
    }

    /**
     * Обробляє повідомлення, надіслані на /app/card/{cardId}
     * та пересилає їх всім підписникам на /topic/card/{cardId}
     *
     * @param cardId ID картки
     * @param message повідомлення для пересилання
     * @return повідомлення для пересилання
     */
    @MessageMapping("/card/{cardId}")
    @SendTo("/topic/card/{cardId}")
    public WebSocketMessage<?> cardMessage(@DestinationVariable Long cardId, WebSocketMessage<?> message) {
        message.setTimestamp(System.currentTimeMillis());
        return message;
    }
}
