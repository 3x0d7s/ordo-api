package com.kyut.ordo.task.event;

import com.kyut.ordo.core.common.event.DomainEvent;
import com.kyut.ordo.task.dto.TaskRead;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

/**
 * Подія оновлення завдання
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TaskUpdatedEvent extends DomainEvent {
    private final Long taskId;
    private final Long cardId;
    private final TaskRead taskData;
    
    public TaskUpdatedEvent(Long taskId, Long cardId, TaskRead taskData) {
        super(UUID.randomUUID().toString(), "TASK_UPDATED");
        this.taskId = taskId;
        this.cardId = cardId;
        this.taskData = taskData;
    }
}
