package com.kyut.ordo.core.websocket.service;

import com.kyut.ordo.core.websocket.dto.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Відправляє повідомлення через веб-сокет до всіх клієнтів, підписаних на певну дошку
     *
     * @param boardId ID дошки
     * @param message повідомлення для відправки
     */
    public <T> void sendBoardMessage(Long boardId, WebSocketMessage<T> message) {
        message.setTimestamp(System.currentTimeMillis());
        messagingTemplate.convertAndSend("/topic/board/" + boardId, message);
    }

    /**
     * Відправляє повідомлення через веб-сокет до всіх клієнтів, підписаних на певний список
     *
     * @param listId ID списку
     * @param message повідомлення для відправки
     */
    public <T> void sendListMessage(Long listId, WebSocketMessage<T> message) {
        message.setTimestamp(System.currentTimeMillis());
        messagingTemplate.convertAndSend("/topic/list/" + listId, message);
    }

    /**
     * Відправляє повідомлення через веб-сокет до всіх клієнтів, підписаних на певну картку
     *
     * @param cardId ID картки
     * @param message повідомлення для відправки
     */
    public <T> void sendCardMessage(Long cardId, WebSocketMessage<T> message) {
        message.setTimestamp(System.currentTimeMillis());
        messagingTemplate.convertAndSend("/topic/card/" + cardId, message);
    }
}
