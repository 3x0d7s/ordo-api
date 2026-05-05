package com.kyut.ordo.dsl;

import com.kyut.ordo.websocket.WebSocketMessage;
import com.kyut.ordo.websocket.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class RuleActions {
    private final WebSocketService webSocketService;

    public void logTaskCreated(Number taskId, String taskTitle) {
        log.info("DSL rule: task created. taskId={}, title={}", taskId, taskTitle);
    }

    public void logTaskUpdated(Number taskId, String taskTitle, Boolean completed) {
        log.info(
                "DSL rule: task updated. taskId={}, title={}, completed={}",
                taskId,
                taskTitle,
                completed
        );
    }

    public void logCardCreated(Number cardId, String cardTitle) {
        log.info("DSL rule: card created. cardId={}, title={}", cardId, cardTitle);
    }

    public void notifyTaskRuleTriggered(Number cardId, String ruleName, String taskTitle) {
        if (cardId == null) {
            return;
        }
        WebSocketMessage<Map<String, Object>> message = WebSocketMessage.<Map<String, Object>>builder()
                .type(WebSocketMessage.WebSocketMessageType.DSL_RULE_TRIGGERED)
                .entityId(String.valueOf(cardId.longValue()))
                .payload(Map.of(
                        "ruleName", ruleName,
                        "taskTitle", taskTitle
                ))
                .build();
        webSocketService.sendCardMessage(cardId.longValue(), message);
    }

    public void notifyCardRuleTriggered(Number boardId, String ruleName, String cardTitle) {
        if (boardId == null) {
            return;
        }
        WebSocketMessage<Map<String, Object>> message = WebSocketMessage.<Map<String, Object>>builder()
                .type(WebSocketMessage.WebSocketMessageType.DSL_RULE_TRIGGERED)
                .entityId(String.valueOf(boardId.longValue()))
                .payload(Map.of(
                        "ruleName", ruleName,
                        "cardTitle", cardTitle
                ))
                .build();
        webSocketService.sendBoardMessage(boardId.longValue(), message);
    }

    public void flagUrgentTask(Number taskId, Number cardId, String taskTitle) {
        if (taskTitle == null) {
            return;
        }
        if (taskTitle.toUpperCase().startsWith("URGENT")) {
            log.warn(
                    "DSL rule: urgent task detected. taskId={}, cardId={}, title={}",
                    taskId,
                    cardId,
                    taskTitle
            );
        }
    }
}
