package com.kyut.ordo.feature.task.event;

import com.kyut.ordo.common.DomainEvent;
import com.kyut.ordo.feature.task.dto.TaskRead;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

/**
 * Подія оновлення завдання
 */
@Data
@Builder
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
