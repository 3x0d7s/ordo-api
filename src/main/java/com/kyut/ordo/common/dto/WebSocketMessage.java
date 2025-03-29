package com.kyut.ordo.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage<T> {
    
    private WebSocketMessageType type;
    private T payload;
    private String entityId;
    private Long timestamp;
    
    public enum WebSocketMessageType {
        CARD_CREATED,
        CARD_UPDATED,
        CARD_DELETED,
        LIST_CREATED,
        LIST_UPDATED,
        LIST_DELETED,
        COMMENT_CREATED,
        COMMENT_UPDATED,
        COMMENT_DELETED,
        TASK_CREATED,
        TASK_UPDATED,
        TASK_DELETED,
        USER_JOINED,
        USER_LEFT
    }
}
