package com.kyut.ordo.dsl;

import com.kyut.ordo.feature.task.dto.TaskRead;
import com.kyut.ordo.feature.task.event.TaskCreatedEvent;
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
        RuleContext context = new RuleContext()
                .put("eventType", event.getEventType())
                .put("taskId", event.getTaskId())
                .put("cardId", event.getCardId())
                .put("taskTitle", taskData != null ? taskData.getTitle() : null)
                .put("taskCompleted", taskData != null && taskData.isCompleted());

        var report = ruleEngine.execute(context);
        log.debug(
                "DSL processed eventType={}, taskId={}, matchedRules={}",
                event.getEventType(),
                event.getTaskId(),
                report.matchedRules()
        );
    }
}
