package com.kyut.ordo.dsl;

import com.kyut.ordo.feature.card.dto.CardWithItsListRead;
import com.kyut.ordo.feature.card.event.CardCreatedEvent;
import com.kyut.ordo.feature.task.dto.TaskRead;
import com.kyut.ordo.feature.task.event.TaskCreatedEvent;
import com.kyut.ordo.feature.task.event.TaskUpdatedEvent;
import dev.kyut.dsl.core.RuleContext;
import dev.kyut.dsl.engine.RuleEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrdoDslEventListener {
    private final RuleEngine ruleEngine;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskCreated(TaskCreatedEvent event) {
        TaskRead taskData = event.getTaskData();
        RuleContext context = baseEventContext(event.getEventType())
                .put("taskId", event.getTaskId())
                .put("cardId", event.getCardId())
                .put("taskTitle", taskData != null ? taskData.getTitle() : null)
                .put("taskCompleted", taskData != null && taskData.isCompleted());

        executeAndLog(event.getEventType(), context);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskUpdated(TaskUpdatedEvent event) {
        TaskRead taskData = event.getTaskData();
        RuleContext context = baseEventContext(event.getEventType())
                .put("taskId", event.getTaskId())
                .put("cardId", event.getCardId())
                .put("taskTitle", taskData != null ? taskData.getTitle() : null)
                .put("taskCompleted", taskData != null && taskData.isCompleted());

        executeAndLog(event.getEventType(), context);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCardCreated(CardCreatedEvent event) {
        CardWithItsListRead cardData = event.getCardData();
        RuleContext context = baseEventContext(event.getEventType())
                .put("cardId", event.getCardId())
                .put("listId", event.getListId())
                .put("boardId", event.getBoardId())
                .put("cardTitle", cardData != null ? cardData.getTitle() : null);

        executeAndLog(event.getEventType(), context);
    }

    private void executeAndLog(String eventType, RuleContext context) {
        var report = ruleEngine.execute(context);
        log.debug("DSL processed eventType={}, matchedRules={}", eventType, report.matchedRules());
    }

    private RuleContext baseEventContext(String eventType) {
        return new RuleContext().put("eventType", eventType);
    }
}
