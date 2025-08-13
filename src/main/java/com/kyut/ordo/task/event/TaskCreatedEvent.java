package com.kyut.ordo.task.event;

import com.kyut.ordo.core.common.event.DomainEvent;
import com.kyut.ordo.task.dto.TaskRead;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

/**
 * Подія створення завдання
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TaskCreatedEvent extends DomainEvent {
    private final Long taskId;
    private final Long cardId;
    private final TaskRead taskData;
    
    public TaskCreatedEvent(Long taskId, Long cardId, TaskRead taskData) {
        super(UUID.randomUUID().toString(), "TASK_CREATED");
        this.taskId = taskId;
        this.cardId = cardId;
        this.taskData = taskData;
    }
}
