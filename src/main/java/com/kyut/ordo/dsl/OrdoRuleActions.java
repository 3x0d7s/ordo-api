package com.kyut.ordo.dsl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrdoRuleActions {
    public void logTaskCreated(Number taskId, String taskTitle) {
        log.info("DSL rule: task created. taskId={}, title={}", taskId, taskTitle);
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
